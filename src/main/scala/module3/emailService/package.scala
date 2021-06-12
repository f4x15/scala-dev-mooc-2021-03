package module3.emailService

import zio.console.Console
import zio.macros.accessible
import zio.{Has, Task, ULayer, URIO, ZIO, ZLayer, console}

  @accessible
  object EmailService {
    type EmailService = Has[EmailService.Service]

    trait Service{
      def sendEmail(email: Email): URIO[Console, Unit]
    }

    val live: ULayer[EmailService] = ZLayer.succeed(new Service {
      override def sendEmail(email: Email): URIO[Console, Unit] =
        console.putStrLn(email.toString)
    })
  }
