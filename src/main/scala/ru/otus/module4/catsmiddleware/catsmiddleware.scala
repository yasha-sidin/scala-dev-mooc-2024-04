package ru.otus.module4.catsmiddleware

import cats.data.{Kleisli, OptionT}
import cats.effect.{IO, IOApp, Resource}
import org.http4s.{AuthedRequest, AuthedRoutes, Http, HttpRoutes, Method, Request, Status, Uri}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s.{Host, Port}
import org.http4s.server.{AuthMiddleware, Router}
import cats.Functor
import cats.effect.kernel.Ref
import org.typelevel.ci.CIStringSyntax
import cats.implicits.toSemigroupKOps

object Restfull {
  val service: HttpRoutes[IO] =
    HttpRoutes.of{
      case GET -> Root/ "hello" / name => Ok(s"bla bla bla")
    }

  val serviceOne: HttpRoutes[IO] = {
    HttpRoutes.of {
      case GET -> Root / "hello1" / name=> Ok("bla1 bla1 bla1")
    }
  }

  val serviceTwo: HttpRoutes[IO] = {
    HttpRoutes.of {
      case GET -> Root / "hello2" / name=> Ok("bla2 bla2 bla2")
    }
  }

  val router = Router(
    "/"->serviceOne,
    "/api" -> serviceTwo,
    "/apiroot" -> service
  )

  val httpApp = router.orNotFound

  val server = EmberServerBuilder
    .default[IO]
    .withHost(Host.fromString("localhost").get)
    .withPort(Port.fromInt(8081).get)
    .withHttpApp(httpApp).build

  //2 add middleware
  val router2 = addResponseMiddleware(Router(
    "/"-> addResponseMiddleware(serviceOne),
    "/api" -> addResponseMiddleware(serviceTwo),
    "/apiroot" -> addResponseMiddleware(service)
  ))
  val httpApp2 = router2.orNotFound
  def addResponseMiddleware[F[_]: Functor](routes: HttpRoutes[F]): HttpRoutes[F] = Kleisli{
    req =>
      val maybeResponse = routes(req)
      maybeResponse.map {
        case Status.Successful(resp) => resp.putHeaders("X-Otus"-> "Hello")
        case other => other
      }
  }
  val server2 = EmberServerBuilder
    .default[IO]
    .withHost(Host.fromString("localhost").get)
    .withPort(Port.fromInt(8081).get)
    .withHttpApp(httpApp2).build

  // add Session
  type Session[F[_]] = Ref[F, Set[String]]
  def serviceSession(sessions: Session[IO]): HttpRoutes[IO] =
    HttpRoutes.of {
      case r@GET -> Root / "hello" =>
        r.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value
            sessions.get.flatMap(users =>
            if (users.contains(name)) Ok(s"Hello, $name")
            else Forbidden("no access")
            )
          case None => Forbidden("no access")
        }
      case PUT -> Root / "login" / name =>
        sessions.update(set => set+name).flatMap(_ => Ok("done"))
    }

  def routerSessions(sessions: Session[IO]): HttpRoutes[IO] =
    addResponseMiddleware(Router("/" -> serviceSession(sessions)))

  val serverSessionServer = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(routerSessions(sessions).orNotFound).build
  } yield s

  // auth
  def routerSessionAuth(sessions: Session[IO]): HttpRoutes[IO] = {
    // <+> combine
    addResponseMiddleware(Router("/" -> (loginService(sessions) <+> serviceAuthMiddleware(sessions)(serviceHelloAuth))))
  }

  def loginService(sessions: Session[IO]): HttpRoutes[IO] =
    HttpRoutes.of {
      case PUT -> Root / "login" / name =>
        sessions.update(set => set + name).flatMap(_=>Ok("done"))
    }
  def serviceHelloAuth: AuthedRoutes[User, IO] = AuthedRoutes.of {
    case GET -> Root / "hello" as user =>
      Ok(s"Hello, ${user.name}")
  }

  final case class User(name: String)
  def serviceAuthMiddleware(sessions:Session[IO]): AuthMiddleware[IO, User] =
    authRoutes =>
      Kleisli{ req =>
        req.headers.get(ci"X-User-Name") match {
          case Some(values) =>
            val name = values.head.value
            for {
              users <- OptionT.liftF(sessions.get)
              results <-
                if (users.contains(name)) authRoutes(AuthedRequest(User(name), req))
                else OptionT.liftF(Forbidden("no access"))
            } yield results
          case None => OptionT.liftF(Forbidden("no access"))
        }
      }

  val serverSessionAuthServer = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(routerSessionAuth(sessions).orNotFound).build
  } yield s

  // clear way


  def routerSessionsAuthClear(sessions: Session[IO]): HttpRoutes[IO] =
    addResponseMiddleware(Router("/" -> (loginService(sessions) <+> serviceAuthMiddleware(sessions)(serviceHelloAuth))))

  val serverSessionAuthServerClear = for {
    sessions <- Resource.eval(Ref.of[IO, Set[String]](Set.empty))
    s <- EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(8081).get)
      .withHttpApp(routerSessionsAuthClear(sessions).orNotFound).build
  } yield s

}

object mainServer extends IOApp.Simple {
  def run(): IO[Unit] = {
    //Restfull.server.use(_ => IO.never)
    //Restfull.server2.use(_ => IO.never)
    //Restfull.serverSessionServer.use(_=>IO.never)
    //Restfull.serverSessionAuthServer.use(_=>IO.never)
    Restfull.serverSessionAuthServerClear.use(_=>IO.never)
  }
}

//tests
object Test extends IOApp.Simple {
  def run: IO[Unit] = {
    val server = Restfull.serviceHelloAuth

    for {
      result <- server(AuthedRequest(Restfull.User("zkjsdfk"), Request(method = Method.GET,
        uri = Uri.fromString("/hello").toOption.get))).value
      _ <- result match {
        case Some(resp) =>
          resp.bodyText.compile.last.flatMap(body => IO.println(resp.status.isSuccess) *> IO.println(body))
        case None => IO.println("fail")
      }
    } yield()
  }
}