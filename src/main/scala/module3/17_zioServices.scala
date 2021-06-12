package module3

import module3.di.DBService
import module3.emailService.EmailService
import module3.emailService.EmailService.EmailService
import module3.userDAO.UserDAO
import module3.userDAO.UserDAO.UserDAO
import module3.userService.{UserID, UserService}
import zio.console.Console
import zio.{Has, RIO, Task, ULayer, ZIO, ZLayer}

object zioServices{


  val app: ZIO[UserService with EmailService with Console, Throwable, Unit] = for{
    _ <- UserService.notifyUser(UserID(1))
  } yield ()

  val appEnv: ZLayer[Any, Throwable, UserService with EmailService] =
    UserDAO.live >>> UserService.live ++ EmailService.live



  zio.Runtime.default.unsafeRun(app.provideSomeLayer[Console](appEnv))


}