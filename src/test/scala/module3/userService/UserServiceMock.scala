package module3.userService

import module3.emailService.EmailService.EmailService
import module3.userService
import zio.test.mock
import zio.{Has, RIO, URLayer, ZLayer}
import zio.test.mock.Mock

object UserServiceMock extends Mock[UserService]{

  object NotifyUser extends Effect[UserID, Throwable, Unit]

  override val compose: URLayer[Has[mock.Proxy], userService.UserService] =
    ZLayer.fromService { proxy =>
       new UserService.Service {
         override def notifyUser(userID: UserID): RIO[EmailService, Unit] = proxy(NotifyUser, userID)
       }
    }
}
