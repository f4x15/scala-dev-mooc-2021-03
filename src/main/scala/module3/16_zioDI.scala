package module3

import zio.{Has, IO, Task, ZIO, ZLayer}
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt
import zio.random.Random

import javax.management.Query
import scala.language.postfixOps

object di {

  type Query[_]
  type DBError
  type QueryResult[_]
  type Email
  type User


  trait DBService{
    def tx[T](query: Query[T]): IO[DBError, QueryResult[T]]
  }

  trait EmailService{
    def makeEmail(email: String, body: String): Task[Email]
    def sendEmail(email: Email): Task[Unit]
  }

  trait LoggingService{
    def log(str: String): Task[Unit]
  }

  val dbService: DBService = ???

  val emailService: EmailService = ???


  val combined: DBService with EmailService = ???



  type MyEnv

  /**
   * Написать эффект который напечатет в консоль приветствие, подождет 5 секунд,
   * сгенерит рандомное число, напечатает его в консоль
   */
  def e1: ZIO[Random with Clock with Console, Nothing, Unit] = for{
    console <- ZIO.environment[Console].map(_.get)
    clock <- ZIO.environment[Clock].map(_.get)
    random <- ZIO.environment[Random].map(_.get)
    _ <- console.putStrLn("Hello")
    _ <- clock.sleep(5 seconds)
    int <- random.nextIntBetween(1, 10)
    _ <- console.putStrLn(int.toString)

  } yield ()


  def e2: ZIO[MyEnv, Nothing, Unit] = ???


  lazy val getUser: ZIO[DBService, Throwable, User] = ???

  lazy val sendMail: ZIO[EmailService, Throwable, Unit] = ???


  /**
   * Эффект, который будет комбинацией двух эффектов выше
   */
  lazy val combined2: ZIO[EmailService with DBService, Throwable, Unit] = getUser *> sendMail


  /**
   * Написать ZIO программу которая выполнит запрос и отправит email
   */
  val queryAndNotify: ZIO[EmailService with DBService, Any, Nothing] = for{
    dbService <- ZIO.environment[DBService]
    emailService <- ZIO.environment[EmailService]
    email <- emailService.makeEmail("", "")
    query: Query[User] = ???
    result <- dbService.tx(query)
    _ <- emailService.sendEmail(email)
  } yield ???



  lazy val services: DBService with EmailService = ???

  def services(emailService: EmailService): EmailService with DBService = ???

  lazy val dBService: DBService = ???

  lazy val emailService2: EmailService = ???

  // provide
  lazy val e3: IO[Any, Nothing] = queryAndNotify.provide(services)

  // provide some
  lazy val e4: ZIO[EmailService, Any, Nothing] = queryAndNotify.provideSome[EmailService](es => services(es))

  // provide
  lazy val e5 = ???

  lazy val servicesLayer: ZLayer[Any, Nothing, DBService with EmailService] = ???

  lazy val dbServiceLayer: ZLayer[Any, Nothing, DBService] = ???

  // provide layer
  lazy val e6 = ???

  // provide some layer
  lazy val e7 = ???

}