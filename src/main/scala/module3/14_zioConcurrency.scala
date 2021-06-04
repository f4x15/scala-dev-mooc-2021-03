package module3

import zio.{Ref, UIO, URIO, ZIO, clock}
import zio.clock.{Clock, sleep}
import zio.console.{Console, putStr, putStrLn}
import zio.duration.durationInt
import zio.internal.Executor

import java.util.concurrent.TimeUnit
import scala.language.postfixOps

object zioConcurrency {


  // эфект содержит в себе текущее время
  val currentTime: URIO[Clock, Long] = clock.currentTime(TimeUnit.SECONDS)


  /**
   * Напишите эффект, который будет считать время выполнения любого эффекта
   */


  def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]) = for{
    start <- currentTime
    r <- zio
    finish <- currentTime
    _ <- putStrLn(s"Running time ${finish - start}")
  } yield r


  val exchangeRates: Map[String, Double] = Map(
    "usd" -> 76.02,
    "eur" -> 91.27
  )

  /**
   * Эффект который все что делает, это спит заданное кол-во времени, в данном случае 1 секунду
   */
  val sleep1Second = ZIO.sleep(1 seconds)

  /**
   * Эффект который все что делает, это спит заданное кол-во времени, в данном случае 1 секунду
   */
  val sleep3Seconds = ZIO.sleep(3 seconds)

  /**
   * Создать эффект который печатает в консоль GetExchangeRatesLocation1 спустя 3 секунды
   */
  lazy val getExchangeRatesLocation1 = sleep3Seconds *> putStrLn("GetExchangeRatesLocation1")

  /**
   * Создать эффект который печатает в консоль GetExchangeRatesLocation2 спустя 1 секунду
   */
  lazy val getExchangeRatesLocation2 = sleep1Second *> putStrLn("GetExchangeRatesLocation2")



  /**
   * Написать эффект котрый получит курсы из обеих локаций
   */
  lazy val getFrom2Locations: ZIO[Console with Clock, Nothing, (Unit, Unit)] =
    getExchangeRatesLocation1 zip getExchangeRatesLocation2


  /**
   * Написать эффект котрый получит курсы из обеих локаций паралельно
   */
  lazy val getFrom2LocationsInParallel = for{
    fiber1 <- getExchangeRatesLocation1.fork
    fiber2 <- getExchangeRatesLocation2.fork
    r1 <- fiber1.join
    r2 <- fiber2.join

  } yield (r1, r2)


  /**
   * Предположим нам не нужны результаты, мы сохраняем в базу и отправляем почту
   */


   val writeUserToDB = sleep1Second *> putStrLn("User in DB")
   val sendMail = sleep1Second *> putStrLn("Mail sent")

  /**
   * Написать эффект котрый сохранит в базу и отправит почту паралельно
   */


  lazy val writeAndSand = for{
    _ <- writeUserToDB.fork
    _ <- sendMail.fork
  } yield ()


  /**
   *  Greeter
   */

  lazy val greeter = for{
    _ <- (sleep1Second *> putStrLn("Hello")).forever.fork
  } yield ()


  /***
   * Greeter 2
   */
  lazy val greeter2 = ZIO.effectTotal(while (true) println("Hello"))

  val greeterApp = for{
    fiber <- greeter2.fork
    _ <- fiber.interrupt
  } yield ()










  /**
   * Прерывание эффекта
   */

   val app3 = for{
     fiber <- getExchangeRatesLocation1.fork
     _ <- getExchangeRatesLocation2
     _ <- fiber.interrupt
     _ <- sleep(3 seconds)
   } yield ()



  /**
   * Получние информации от сервиса занимает 1 секунду
   */
  def getFromService(ref: Ref[Int]) = for {
    count <- ref.getAndUpdate(_ + 1)
    _ <- putStrLn(s"GetFromService - ${count}") *> ZIO.sleep(1 seconds)
  } yield ()

  /**
   * Отправка в БД занимает в общем 5 секунд
   */
  def sendToDB(ref: Ref[Int]): ZIO[Clock with Console, Exception, Unit] = for {
    count <- ref.getAndUpdate(_ + 1)
    _ <- ZIO.sleep(5 seconds) *> putStrLn(s"SendToDB - ${count}")
  } yield ()


  /**
   * Написать программу, которая конкурентно вызывает выше описанные сервисы
   * и при этом обеспечивает сквозную нумерацию вызовов
   */
  lazy val app1 = ???


  /**
   *  Concurrent operators
   */

   val _ = getExchangeRatesLocation1 zipPar getExchangeRatesLocation2
   val _ = ZIO.foreachPar(List("1", "2", "3"))(str => putStrLn(str))
   val _ = getExchangeRatesLocation1 race getExchangeRatesLocation2


  /**
   * Lock
   */


  // Правило 1
  lazy val doSomething: UIO[Unit] = ???
  lazy val doSomethingElse: UIO[Unit] = ???

  lazy val executor: Executor = ???

  val eff = for{
    f <- doSomething.fork
    _ <- doSomethingElse
    _ <- f.join
  } yield ()

  val result = eff.lock(executor)



  // Правило 2
  lazy val executor1: Executor = ???
  lazy val executor2: Executor = ???



  val eff2 = for{
    f <- doSomething.lock(executor2).fork
    _ <- doSomethingElse
    _ <- f.join
  } yield ()

  val result2 = eff2.lock(executor1)


  /**
   * простая гонка эффектов
   */
  val res1: URIO[Clock, Int] = ZIO.sleep(3.second).as(4)

  val res2: URIO[Clock, Int] = ZIO.sleep(1.second).as(7)




}