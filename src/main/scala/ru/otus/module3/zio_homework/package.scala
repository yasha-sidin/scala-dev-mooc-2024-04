package ru.otus.module3

import zio.{RIO, ZIO}

import scala.language.postfixOps
import zio.console._
import zio.random._

import scala.io.StdIn

package object zio_homework {
  /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в консоль угадал или нет. Подумайте, на какие наиболее простые эффекты ее можно декомпозировать.
   */


  def inputNumberEffect(start: Int, end: Int): ZIO[Console, Throwable, Int] = for {
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

  def doWhile[R, E, A](effect: ZIO[R, E, A], op: A => A, expr: A => Boolean): ZIO[R, E, A] = {
    effect.flatMap(res => {
      val newRes = op(res)
      if(expr(newRes)) effect.as(newRes) else doWhile(effect.as(newRes), op, expr)
    })
  }


  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */


  def loadConfigOrDefault = ???


  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   * 4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   * Используйте сервис zio Random
   */
  lazy val eff = ???

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects = ???


  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекции "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app = ???


  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

  lazy val appSpeedUp = ???


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * можно было использовать аналогично zio.console.putStrLn например
   */


  /**
   * 6.
   * Воспользуйтесь написанным сервисом, чтобы созадть эффект, который будет логировать время выполнения прогаммы из пункта 4.3
   *
   *
   */

  lazy val appWithTimeLogg = ???

  /**
   *
   * Подготовьте его к запуску и затем запустите воспользовавшись ZioHomeWorkApp
   */

  lazy val runApp = ???

}
