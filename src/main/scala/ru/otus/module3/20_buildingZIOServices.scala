package ru.otus.module3

import ru.otus.module3.emailService.EmailService
import ru.otus.module3.userDAO.UserDAO
import ru.otus.module3.userService.{UserID, UserService}
import zio.RIO
import zio.console.Console


object buildingZIOServices{


  lazy val app: RIO[UserService with EmailService with Console, Unit] =
    UserService.notifyUser(UserID(10))

  lazy val env = UserDAO.live >>> UserService.live ++ EmailService.live

  def main(args: Array[String]): Unit = {
    zio.Runtime.default.unsafeRun(app.provideSomeLayer[Console](env))
  }
}