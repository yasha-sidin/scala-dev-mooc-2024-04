package ru.otus.module4.homework

import cats.data.Kleisli
import cats.effect.{IO, IOApp, Resource}
import cats.implicits.{catsSyntaxApply, catsSyntaxTuple3Semigroupal}
import com.comcast.ip4s.{Host, Port}
import org.http4s.{HttpRoutes, Request, Response, Uri}
import org.http4s.dsl.io._
import fs2.io.file.Files
import fs2.io.file._
import fs2.{Chunk, Pure, Stream, io, text}
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import ru.otus.module4.homework.RestStream.{buildEnv, server}
import ru.otus.module4.homework.StreamServer.pngPathString

import scala.concurrent.duration._
import java.nio.file.{Path, Paths}
import scala.language.postfixOps

object RestStream {
  implicit class RequestValidation(string: String) {
    def toValidOption: Option[Int] =
      string.toIntOption.flatMap(res => if (res <= 0) None else Some(res))
  }

  final case class Environment(path: Path)

  val buildEnv: String => Resource[IO, Environment] =
    (pathString: String) => Resource.make(
      IO.delay(Paths.get(pathString))
    )(_ => IO.unit).map(Environment)

  val service: Environment => HttpRoutes[IO] =
    (env: Environment) => HttpRoutes.of {
      case POST -> Root / "slow" / chunk / total / time =>
        val chunkOption = chunk.toValidOption
        val totalOption = total.toValidOption
        val timeOption = time.toValidOption
        val tuple = (chunkOption, totalOption, timeOption).tupled
        tuple match {
          case Some(res) =>
            val baseStream = Files[IO]
              .readAll(env.path, res._2)
              .take(res._2)
              .chunkLimit(res._1)
            val firstChunk = baseStream.take(1)
            val secondChunk = baseStream
              .drop(1)
              .metered(res._3 seconds)
            val stream = firstChunk.flatMap(Stream.chunk) ++ secondChunk.flatMap(Stream.chunk)
            Ok(stream)
          case None =>
            BadRequest()
        }
    }

  val httpApp: Environment => Kleisli[IO, Request[IO], Response[IO]] =
    (env: Environment) => service(env).orNotFound

  val server: Environment => Resource[IO, Server] =
    (env: Environment) => for {
      s <- EmberServerBuilder
        .default[IO]
        .withHost(Host.fromString("localhost").get)
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(httpApp(env)).build
    } yield s
}

object StreamServer extends IOApp.Simple {
  val pngPathString: String = "src//main//resources//ai-generated.png"

  override def run: IO[Unit] = buildEnv(pngPathString).use { env =>
    server(env).use(_ => IO.never)
  }
}

object ServerStreamTest extends IOApp.Simple {
  val builder: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  val request: Request[IO] = Request[IO](uri = Uri.fromString("http://localhost:8080/slow/12/100/2").toOption.get, method = POST)

  val result: IO[Unit] = builder.use { client =>
    client.stream(request).flatMap(response => {
      if (response.status.isSuccess) {
        response.body
          .chunks
          .evalMap(chunk => IO.println(s"Received chunk with ${chunk.size} bytes"))
      } else {
        Stream.eval(IO.println(response.status.code) *> IO.raiseError(new Exception("error")))
      }
    }).compile.drain
  }

  def run(): IO[Unit] = {
    buildEnv(pngPathString).use { env =>
      server(env).use(_ =>
        result *> IO.unit
      )
    }
  }
}
