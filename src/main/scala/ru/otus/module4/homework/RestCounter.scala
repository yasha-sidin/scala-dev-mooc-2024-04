package ru.otus.module4.homework

import cats.data.Kleisli
import cats.effect.{IO, IOApp, Ref, Resource}
import com.comcast.ip4s.{Host, Port}
import io.circe.generic.auto._
import org.http4s.{HttpRoutes, Request, Response, Uri}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.client.Client
import org.http4s.dsl.io.{->, /, GET, Ok, Root}
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import ru.otus.module4.homework.RestCounter._

case class Counter(counter: Int)

object RestCounter {
  final case class Environment(counter: Ref[IO, Counter])

  def buildEnv: Resource[IO, Environment] =
    Resource.make(
      Ref.of[IO, Counter](Counter(0))
    )(_ => IO.unit).map(Environment)

  val service: Environment => HttpRoutes[IO] =
    (env: Environment) => HttpRoutes.of {
      case POST -> Root / "counter" =>
        env
          .counter
          .updateAndGet(counterRef =>
            counterRef.copy(counter = counterRef.counter + 1)
          ).flatMap(Ok(_))
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

object ServerCounter extends IOApp.Simple {
  def run(): IO[Unit] = {
    buildEnv.use { env =>
      server(env).use(_ => IO.never)
    }
  }
}

object ServerCounterTest extends IOApp.Simple {
  val builder: Resource[IO, Client[IO]] = EmberClientBuilder.default[IO].build
  val request: Request[IO] = Request[IO](uri = Uri.fromString("http://localhost:8080/counter").toOption.get, method = POST)

  val result: Resource[IO, Response[IO]] = for {
    client <- builder
    response <- client.run(request)
  } yield response

  def validateResponse(response: Resource[IO, Response[IO]]): IO[Counter] = {
    response.use { resp =>
      if (resp.status.isSuccess)
        resp.as[Counter]
      else
        IO.raiseError(new Exception("error"))
    }
  }

  def run(): IO[Unit] = {
    buildEnv.use { env =>
      server(env).use(_ =>
        validateResponse(result).flatMap(counter => IO.println(counter.counter == 1)) *>
          validateResponse(result).flatMap(counter => IO.println(counter.counter == 2)) *>
          validateResponse(result).flatMap(counter => IO.println(counter.counter == 3)) *>
          IO.unit)
    }
  }
}
