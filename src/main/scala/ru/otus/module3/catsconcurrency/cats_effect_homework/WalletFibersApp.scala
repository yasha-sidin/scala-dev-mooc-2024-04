package ru.otus.module3.catsconcurrency.cats_effect_homework

import cats.effect.{Deferred, IO, IOApp, Resource}
import cats.implicits._

import scala.concurrent.duration._

import scala.language.postfixOps

// Поиграемся с кошельками на файлах и файберами.

// Нужно написать программу где инициализируются три разных кошелька и для каждого из них работает фоновый процесс,
// который регулярно пополняет кошелек на 100 рублей раз в определенный промежуток времени. Промежуток надо сделать разный, чтобы легче было наблюдать разницу.
// Для определенности: первый кошелек пополняем раз в 100ms, второй каждые 500ms и третий каждые 2000ms.
// Помимо этих трёх фоновых процессов (подсказка - это файберы), нужен четвертый, который раз в одну секунду будет выводить балансы всех трех кошельков в консоль.
// Основной процесс программы должен просто ждать ввода пользователя (IO.readline) и завершить программу (включая все фоновые процессы) когда ввод будет получен.
// Итого у нас 5 процессов: 3 фоновых процесса регулярного пополнения кошельков, 1 фоновый процесс регулярного вывода балансов на экран и 1 основной процесс просто ждущий ввода пользователя.

// Можно делать всё на IO, tagless final тут не нужен.

// Подсказка: чтобы сделать бесконечный цикл на IO достаточно сделать рекурсивный вызов через flatMap:
// def loop(): IO[Unit] = IO.println("hello").flatMap(_ => loop())
object WalletFibersApp extends IOApp.Simple {
  final case class Environment(startWork: Deferred[IO, Unit])

  def buildEnv: Resource[IO, Environment] =
    Resource.make(
      IO.println("Start deferred...") *> Deferred[IO, Unit]
    )(_ => IO.println("...Stop deferred")).map(deferred => Environment(deferred))

  def program(env: Environment): IO[Unit] = for {
    _ <- IO.println("Press any key to stop...")
    wallet1 <- Wallet.fileWallet[IO]("1")
    wallet2 <- Wallet.fileWallet[IO]("2")
    wallet3 <- Wallet.fileWallet[IO]("3")
    fiber1 = IO.println("Fiber1 is ready.") *> env.startWork.get *>
      IO.println("Fiber1 is started.") *> (wallet1.topup(100.0) *> IO.sleep(100 millisecond)).foreverM
    f1 <- fiber1.onCancel(IO.println("Fiber1 has been stopped")).start
    fiber2 = IO.println("Fiber2 is ready.") *> env.startWork.get *>
      IO.println("Fiber2 is started.") *> (wallet2.topup(100.0) *> IO.sleep(500 millisecond)).foreverM
    f2 <- fiber2.onCancel(IO.println("Fiber2 has been stopped")).start
    fiber3 = IO.println("Fiber3 is ready.") *> env.startWork.get *>
      IO.println("Fiber3 is started.") *> (wallet3.topup(100.0) *> IO.sleep(2000 millisecond)).foreverM
    f3 <- fiber3.onCancel(IO.println("Fiber3 has been stopped")).start
    fiber4 = IO.println("Fiber4(reading) is ready.") *> env.startWork.get *>
      IO.println("Fiber4(reading) is started.") *>
      (IO.sleep(1000 millisecond) *> wallet1.balance.flatMap(balance => IO.println(s"Wallet1: $balance")) *>
      wallet2.balance.flatMap(balance => IO.println(s"Wallet2: $balance")) *>
      wallet3.balance.flatMap(balance => IO.println(s"Wallet3: $balance"))).foreverM
    f4 <- fiber4.onCancel(IO.println("Fiber4 has been stopped")).start
    _ <- env.startWork.complete()
    _ <- IO.readLine
    _ <- f1.cancel
    _ <- f2.cancel
    _ <- f3.cancel
    _ <- f4.cancel
  } yield ()

  def run: IO[Unit] = buildEnv.use { env =>
    program(env)
  }
}
