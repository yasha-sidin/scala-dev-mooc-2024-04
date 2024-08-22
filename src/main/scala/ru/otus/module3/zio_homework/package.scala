package ru.otus.module3

import ru.otus.module3.zio_homework.EffectRunningTimeService.Service
import ru.otus.module3.zio_homework.config.AppConfig
import zio.clock.Clock
import zio.{Has, RIO, UIO, ULayer, ZIO, ZLayer}

import scala.language.postfixOps
import zio.console._
import zio.random._
import zio.duration._
import zio.macros.accessible

import java.io.IOException
import scala.annotation.tailrec
import scala.io.StdIn

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в консоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */


  private def inputNumberEffect(start: Int, end: Int): ZIO[Console, Throwable, Int] = for {
    _ <- putStr("Введите число: ")
    numberString <- ZIO.effect(StdIn.readLine())
    number <- ZIO.effect(numberString.toInt).orElse(
      putStrLn("Неверный формат числа. Попробуйте еще раз.") *> inputNumberEffect(start, end)
    ).flatMap(
      number =>
        if (start <= number && number <= end)
          ZIO.succeed(number)
        else
          putStrLn(s"Число должно быть от $start до $end. Попробуйте еще раз.") *>
            inputNumberEffect(start, end)
    )
  } yield number


  lazy val guessProgram: (Int, Int) => RIO[Console with Random, Unit] = (start: Int, end: Int) =>
    for {
      random <- nextIntBetween(start, end + 1)
      _ <- putStrLn(s"Угадайте число от $start до $end")
      number <- inputNumberEffect(start, end)
      isEqual <- ZIO.succeed(number == random)
      _ <- putStrLn(if (isEqual) "Правильно!" else "Неверно!")
    } yield ()

  /**
   * 2. реализовать функцию doWhile (общего назначения), которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   *
   */

  def doWhile[R, E, A](effect: ZIO[R, E, A], expr: A => Boolean): ZIO[R, E, Unit] = {
    effect.flatMap(res => {
      doWhile(effect, expr).unless(expr(res))
    })
  }


  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */


  private def defaultConfig: UIO[AppConfig] = ZIO.succeed(AppConfig("localhost", "5000"))

  def loadConfigOrDefault: ZIO[Console, IOException, Unit] =
    config
      .load
      .orElse(defaultConfig)
      .flatMap(config => putStrLn(config.toString))


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff: RIO[Random with Clock, Int] = nextIntBetween(0, 11).delay(1 second)

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  private def fillListEqualsObjects[A](unit: A, count: Int): List[A] = {
    @tailrec
    def helperDef[B](unit: B, count: Int, list: List[B]): List[B] = {
      count match {
        case x if x <= 0 => list
        case _ => helperDef(unit, count - 1, unit :: list)
      }
    }

    helperDef(unit, count, Nil)
  }

  lazy val effects: List[RIO[Random with Clock, Int]] = fillListEqualsObjects(eff, 10)


  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекции "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  def printEffectRunningTime[R, E, A](effect: ZIO[R, E, A]): ZIO[Console with R, E, A] = for {
    start <- putStrLn("Start...").as(System.currentTimeMillis()).orDie
    r <- effect
    end <- putStrLn("...End").as(System.currentTimeMillis()).orDie
    res <- ZIO.succeed((end - start) / 1000)
    _ <- putStrLn(s"Duration: $res second(s)").orDie
  } yield r


  lazy val app: ZIO[Console with Random with Clock, Any, Unit] = printEffectRunningTime {
    for {
      sum <- ZIO.mergeAll(effects)(0)((previous, next) => previous + next)
      _ <- putStrLn(s"Sum: $sum")
    } yield ()
  }


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp: ZIO[Console with Random with Clock, Any, Unit] = printEffectRunningTime {
    for {
      sum <- ZIO.mergeAllPar(effects)(0)((previous, next) => previous + next)
      _ <- putStrLn(s"Sum: $sum")
    } yield ()
  }


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * можно было использовать аналогично zio.console.putStrLn например
   */

  type EffectRunningTimeService = Has[Service]

  object EffectRunningTimeService {
    trait Service {
      def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[R with Clock with Console, E, A]
    }

    class ServiceImpl extends Service {
      override def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[R with Clock with Console, E, A] =
        zioConcurrency.printEffectRunningTime(zio)
    }

    val live: ULayer[EffectRunningTimeService] = ZLayer.succeed(new ServiceImpl)

    def printEffectRunningTime[R, E, A](
                                         zio: ZIO[R, E, A]
                                       ): ZIO[EffectRunningTimeService with R with Clock with Console, E, A] =
      ZIO.accessM(_.get.printEffectRunningTime(zio))
  }

  /**
   * 6.
   * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
   *
   *
   */

  lazy val appWithTimeLogg: ZIO[EffectRunningTimeService with Console with Random with Clock, Any, Unit] =
    EffectRunningTimeService.printEffectRunningTime(app)

  /**
   *
   * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
   */

  lazy val runApp: ZIO[Console with Random with Clock, Any, Unit] =
    appWithTimeLogg.provideSomeLayer[Console with Random with Clock](EffectRunningTimeService.live)

}
