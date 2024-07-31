package ru.otus.module3.zio_homework

import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.random.Random
import zio.{ExitCode, URIO, ZIO}

object ZioHomeWorkApp extends scala.App {

  def task(num: Int) = for {
    res <- ZIO.effect(num)
    _ <- putStrLn(res.toString)
  } yield res

  zio.Runtime.default.unsafeRun(doWhile(task(0), (el: Int) => el + 1,  (el: Int) => el == 6).flatMap(res => putStrLn(res.toString)))
}
