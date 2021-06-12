package module3

import module3.emailService.EmailService.{EmailService}
import module3.emailService.{Email, EmailAddress, EmailService, Html}
import module3.userDAO.UserDAO
import module3.userDAO.UserDAO.UserDAO
import module3.userService.UserID
import zio.console.Console
import zio.macros.accessible
import zio.{Has, RIO, Task, ZIO, ZLayer}


package object userService{

  type UserService = Has[UserService.Service]

  @accessible
  object UserService{

    trait Service{
      def notifyUser(userID: UserID): RIO[EmailService with Console, Unit]
    }

    class ServiceImpl(userDAO: UserDAO.Service) extends Service {
      override def notifyUser(userID: UserID): RIO[EmailService with Console, Unit] = for {
        user <- userDAO.findBy(userID)
        email = Email(EmailAddress("test@test.com"), Html("Hello there"))
        _ <- EmailService.sendEmail(email)
      } yield ()
    }

    val live = ZLayer.fromService[UserDAO.Service, UserService.Service](dao => new ServiceImpl(dao))
  }

}

