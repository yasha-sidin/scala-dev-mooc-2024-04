package ru.otus.module3.catsconcurrency.cats_effect_homework

import cats.effect.{Resource, Sync}
import cats.implicits._
import Wallet._

import java.nio.file.Files._
import java.nio.file.{Files, Path, Paths}

// DSL управления электронным кошельком
trait Wallet[F[_]] {
  // возвращает текущий баланс
  def balance: F[BigDecimal]

  // пополняет баланс на указанную сумму
  def topup(amount: BigDecimal): F[Unit]

  // списывает указанную сумму с баланса (ошибка если средств недостаточно)
  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]]
}

// Игрушечный кошелек который сохраняет свой баланс в файл
// todo: реализовать используя java.nio.file._
// Насчёт безопасного конкуррентного доступа и производительности не заморачиваемся, делаем максимально простую рабочую имплементацию. (Подсказка - можно читать и сохранять файл на каждую операцию).
// Важно аккуратно и правильно завернуть в IO все возможные побочные эффекты.
//
// функции которые пригодятся:
// - java.nio.file.Files.write
// - java.nio.file.Files.readString
// - java.nio.file.Files.exists
// - java.nio.file.Paths.get
final class FileWallet[F[_] : Sync](id: WalletId) extends Wallet[F] {
  private def filePath(idWallet: WalletId): F[Path] = Sync[F].delay {
    val path = Paths.get(s"./src/main/resources/wallet/$idWallet")
    if (!Files.exists(path)) {
      Files.createFile(path)
      writeString(path, "0.0")
    }
    path
  }

  def balance: F[BigDecimal] = filePath(id).map(path => BigDecimal(readString(path)))

  def topup(amount: BigDecimal): F[Unit] = for {
    value <- balance
    _ <- filePath(id).map(path =>
        writeString(path, (value + amount).toString)
      )
  } yield ()


  def withdraw(amount: BigDecimal): F[Either[WalletError, Unit]] = balance.flatMap { value =>
    val operation = value - amount
    if (operation >= 0)
      filePath(id).map(path => Right(writeString(path, operation.toString)))
    else Sync[F].delay {
      Left(BalanceTooLow)
    }
  }
}

object Wallet {

  // todo: реализовать конструктор
  // внимание на сигнатуру результата - инициализация кошелька имеет сайд-эффекты
  // Здесь нужно использовать обобщенную версию уже пройденного вами метода IO.delay,
  // вызывается она так: Sync[F].delay(...)
  // Тайпкласс Sync из cats-effect описывает возможность заворачивания сайд-эффектов
  def fileWallet[F[_] : Sync](id: WalletId): F[Wallet[F]] = Sync[F].delay {
    new FileWallet[F](id)
  }

  type WalletId = String

  sealed trait WalletError

  case object BalanceTooLow extends WalletError
}
