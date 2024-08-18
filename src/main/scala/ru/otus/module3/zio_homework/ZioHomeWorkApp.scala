package ru.otus.module3.zio_homework

import zio.clock.Clock
import zio.console.{Console, putStr, putStrLn}
import zio.random.{Random, nextIntBetween}
import zio.{ExitCode, RIO, URIO, ZIO}

object ZioHomeWorkApp extends scala.App {

//  // 1.
//  zio.Runtime.default.unsafeRun(guessProgram(1, 3).forever)

//  // 2.
//  def effect: RIO[Console with Random, Int] = for {
//    num <- nextIntBetween(0, 100)
//    res <- putStrLn(num.toString) *> ZIO.effect(num)
//  } yield res
//
//  zio.Runtime.default.unsafeRun(putStrLn("Start...") *> doWhile(effect, (res: Int) => res == 10) *> putStrLn("...End"))

//  // 3.
//  zio.Runtime.default.unsafeRun(loadConfigOrDefault)

//  // 4.3
//  zio.Runtime.default.unsafeRun(app)

//  // 4.4
//  zio.Runtime.default.unsafeRun(appSpeedUp)

  // 6
  zio.Runtime.default.unsafeRun(runApp)
}
