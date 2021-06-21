package module3

import zio.{IO, Task, UIO, URIO}
import zio.console.{Console, putStrLn}

object zioErrorHandling {

  /*
    Ошибки могут быть те которые мы знаем как обрабатывать и те которые не знаем как
    error - те которые знаем
    defect - те которые не знаем (Аннашка масло пролила из серии)

    какой выбрать бывает далеко не сразу понятно. Зависит от контекста.
   */

  //Модель которая описывает ошибки.
  sealed trait Cause[+E]

  object Cause {

    // когда с error
    final case class Fail[E](e: E) extends Cause[E]

    // когда мы падаем c defect
    final case class Die(t: Throwable) extends Cause[Nothing]

  }

  // дорабатываем наш игрушечный ЗИО чтобы добавить канал ошибки
  case class ZIO[-R, +E, +A](run: R => Either[E, A]) {
    self =>

    // foldM в терминах исполнения модели
    //  Результат выполнение фолда ЗИО, просто выполняется разное в зависимости от того что
    //  нам пришло. Если лефт то применяем к нму failure, если райт то success
    def foldM[R1 <: R, E1, B](
        failure: E => ZIO[R1, E1, B],
        success: A => ZIO[R1, E1, B]
    ): ZIO[R1, E1, B] =
      ZIO(r => self.run(r).fold(failure, success).run(r))
    // в терминах себя он позволяет реалиовывать другие операторы

    // откуда взяли e и v?
    def orElse[R1 <: R, E1, A1 >: A](
        other: ZIO[R1, E1, A1]
    ): ZIO[R1, E1, A1] = {
      foldM(
        _ => other,
        v => ZIO(_ => Right(v)) // Взялось из того что это функции
      )
    }

    /**
      * Конвертируем текущий ZIO в option
      *
      * Реализовать метод, котрый будет игнорировать ошибку в случае падения,
      * а в качестве результата возвращать Option
      *
      *  возвращает в качестве результата ZIO, но в качестве результата option:
      *  Если ошибка - то успешный ZIO от None, если успех то успешный ZIO от Some(v)
      */
    def option: ZIO[R, Nothing, Option[A]] = {
      foldM( // с помощью него мы определеяем когда успешный а когда нет - получается
        //  pattern matching не нужен.
        _ => ZIO(r => Right(None)),
        s => ZIO(r => Right(Some(s)))
      )
    }

    /**
      * Реализовать метод, котрый будет работать с каналом ошибки
      *
      * Позволяет изменить тип ошибкт в канале. Позволяет применить трансофрмацию к ошибке
      */
    def mapError[E1](f: E => E1): ZIO[R, E1, A] =
      foldM(
        e => ZIO(_ => Left(f(e))),
        v => ZIO(_ => Right(v))
      )

  }

  sealed trait UserRegistrationError

  case object InvalidEmail extends UserRegistrationError

  case object WeakPassword extends UserRegistrationError

  lazy val checkEmail: IO[InvalidEmail.type, String] = ???
  lazy val checkPassword: IO[WeakPassword.type, String] = ???

  // компилятор нашел общего предка поэтому так
  lazy val userRegistrationCheck
      : zio.ZIO[Any, UserRegistrationError, (String, String)] =
    checkEmail.zip(checkPassword)

  lazy val io1: IO[String, String] = ???

  lazy val io2: IO[Int, String] = ???

  /**
    * 1. Какой будет тип на выходе, если мы скомбинируем эти два эффекта с помощью zip
    */

  // компилятор так же вывел обзнго предка по каналу ошибки но мы потеряли знания
  //  про тип ошибок и это грусть
  val _: zio.ZIO[Any, Any, (String, String)] = io1.zip(io2)

  /**
    * Можем ли мы как-то избежать потерю информации об ошибке, в случае композиции?
    */

  // q - получается foldM - это такой аналог patternMachinga
  // q - а если требуется объединить больше 2х?

  // 0. нужно не потерять ошибку => модифицировать канал ошибки AND объединить эффекты
  // 1. передаем ошибку которая есть в этом канале
  //  2. делаем zip
  lazy val io3: zio.ZIO[Any, Either[String, Int], (String, String)] =
    io1.mapError(Left(_)).zip(io2.mapError(Right(_)))
  // почему right?  // String            // Int
  //  тк ZIO это либо Left либо Right
  // if io1 effects зафейленный мы выполним mapError и получим left от стринеги
  //  и второй эффект в данном случае выполняться не будет - цепочка прерветься
  // Если еерое во втором эффекте - то на выходе будет лежать right(Int)
  //  что логично учитывая сигнатуру метода

  io3.fork

  // Sample:
  def either: Either[String, Int] = ???
  def errorToErrorCode(str: String): Int = ???
  lazy val effFromEither: IO[String, Int] = zio.ZIO.fromEither(either)

  /**
    * Залогировать ошибку effFromEither, не меняя ее тип и тип возвращаемого значения
    */
  // работает с ошибкой как mapError но при этом не меняет сигнатуру нашего эффекта
  //  например с помощью него можно логировать
  // ZIO[Console, String, Int] - не поменялс я тип ошибки, не поменялся тип возвращаемого значения
  //  но появилась зависимость: консоль. Вместе с ЗИО появлилось зависимость
  lazy val _: zio.ZIO[Console, String, Int] = effFromEither.tapError { str =>
    putStrLn(str)
  }

  /**
    * Изменить ошибку effFromEither
    */
  // mapError - меняет тип канала ошибки String->Int (функция которую мы передали так сделала)
  lazy val _: zio.ZIO[Any, Int, Int] = effFromEither.mapError(errorToErrorCode)

  /**
    * ротация типов
    *  Есть тип: effFromEither
    */
  // тут ошибка перекочует ИЗ канала ошибки в канал результата
  //  избавились от канала ошибки в принципе, но при этом в качестве возвращаемого
  //  значения получили Either
  lazy val effEitherErrorOrResult: UIO[Either[String, Int]] =
    effFromEither.either

  // Вернуть effEitherErrorOrResult обратно
  lazy val _: zio.IO[String, Int] = effEitherErrorOrResult.absolve

  // либо имеем дело с ZIO который потенциально паджает с какой то ошибкой
  // либо ZIO который не падает, но иметь дело с Either в качестве возвращаемого типа

  // orDie оператор

  type User = String
  type UserId = Int

  sealed trait NotificationError
  case object NotificationByEmailFailed extends NotificationError
  case object NotificationBySMSFailed extends NotificationError

  def getUserById(userId: UserId): Task[User] = ???

  def sendEmail(
      user: User,
      email: String
  ): IO[NotificationByEmailFailed.type, Unit] = ???

  def sendSMS(
      user: User,
      phone: String
  ): IO[NotificationBySMSFailed.type, Unit] = ???

  // уведомление пользователя
  //  на нужен for-для выстраивания цепочки операций
  def sendNotification(userId: UserId): IO[NotificationError, Unit] =
    for {
      user <- getUserById(userId).orDie // тут прячется throwable ошибка,
      // через orDie мы превращаем ошибку в дефект
      _ <- sendEmail(user, "email text") // тут бизнес-ошибки
      _ <- sendSMS(user, "sms text") // тут бизнес-ошибки
    } yield ()
  // те компилятор пытается вывезти общий тип -> Object
  // В текущем контексте при падении getUserById -> мы больше ничего сделать не можем
  //  те. это для нас Defect!
  // orDie - превращает Throwable в defect!
  // Если не будет юзера - мы упадем с uncheked error тк ничего больше туту сделать мы
  //  не можем и для нас это Defect
  // orDie - получаем UIO, и Nothing - подтип всех типов => у нас тут все хорошо
  //  те эффект становиться Unfallable -> не падает с ошибками, а если он упадет, то
  //  это будет Unchecked error
  // Тк выполнение последовательное -> если email упадет, то sms мы не получим.
  // тут зависит как нам нужно в логике чтобы случилось.

  // one or another
  def sendNotificationExactlyOnes(
      userId: UserId
  ): IO[NotificationError, Unit] = {
    getUserById(userId).orDie // map to defect
      .flatMap(u => sendEmail(u, "email").orElse(sendSMS(u, "sms")))
  }

  // the both on catch error
  def sendNotificationTheBoth(userId: UserId): IO[NotificationError, Unit] = {
    getUserById(userId).orDie // map to defect
      .flatMap(u => sendEmail(u, "email").zipRight(sendSMS(u, "sms")))
  }
}
