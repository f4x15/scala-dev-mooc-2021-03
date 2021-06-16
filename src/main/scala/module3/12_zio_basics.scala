package module3

import module3.zioOperators.readLine
import zio.clock.{Clock, nanoTime}
import zio.console.{Console, getStrLn}
import zio.{IO, RIO, Task, UIO, URIO, ZIO}

import java.io.IOException
import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Try

/** **
  *
  * ZIO[-R, +E, +A] ----> R => Either[E, A]
  *  `ZIO mental model`: it is some fucntion from some `context`-R into result: success or some error
  *
  * this is `type constructor` with three parameters:
  * R - context
  * E - errors
  * A - success result type
  */

object toyModel {

  // mental model:
  val f: String => Either[Throwable, Int] = ???

  // for get some result we must send some parameter.
  val res: Either[Throwable, Int] = f("testString")

  // Either: or left or right

  /**
    * Используя executable encoding реализуем свой zio
    */

  case class ZIO[-R, +E, +A](run: R => Either[E, A]) { self =>
    // For run our effect, we must call `run`-method.
    //  For call this method we must sent R-parameter, for receipt appropriate result.
    // => call by name for laziness

    def map[B](f: A => B): ZIO[R, E, B] = ZIO(r => self.run(r).map(f))

    def flatMap[R1 <: R, E1 >: E, B](f: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      ZIO(r => self.run(r).fold(ZIO.fail, f).run(r))
    // r - is partial functions?
    // type bounds because return other ZIO not our.
    // 1. ZIO on result.
    // 2. for return Either return.
    //  operators are realised in terms of running of models: realise flatMap in terms of execute of
    //  our models: `self.run` -> return Either[E, A]
    // 3. fold - take two lambdas: E->C

  }

  /**
    * Реализуем конструкторы под названием effect и fail
    */

  object ZIO {

    // any may be any and it is here ok
    // not nothing, потому что его экземпляра не существует и нельзя было создать. Поэтому Throwable
    def effect[A](value: => A): ZIO[Any, Throwable, A] = {

      try {
        ZIO(_ => Right(value))
      } catch {
        case e => ZIO(_ => Left(e))
      }
    }

    def fail[E](e: E): ZIO[Any, E, Nothing] = ZIO(_ => Left(e))
  }

  /** *
    * Напишите консольное echo приложение с помощью нашего игрушечного ZIO
    */

  val echo: ZIO[Any, Throwable, Unit] = for {
    str <- ZIO.effect(StdIn.readLine())
    _ <- ZIO.effect(println(str))
  } yield ()

}

object zioTypeAliases {
  type Error
  type Environment

  // ZIO[-R, +E, +A]

  // ZIO[Any, Nothing, Nothing]

  // Any - наплевать какой environment
  lazy val _: Task[Int] = ??? // ZIO[Any, THROWABLE, Int]   // Future analogue.
  lazy val _: IO[Error, Int] = ??? // ZIO[Any, Error, Int]
  lazy val _: RIO[Environment, Int] = ??? // ZIO[Env, THROWABLE, Int]
  lazy val _: URIO[Environment, Int] = ??? // ZIO[Env, Nothing, Int]
  lazy val _: UIO[Int] = ??? // ZIO[Any, Nothing, Int]

  /*
    U - Unfatable  Nothing - не падает
    R - enviRonment
   */

}

object zioConstructors {

  // константа
  val _: UIO[Int] = ZIO.succeed(7) // unfallable (не падает) :- UIO

  // любой эффект
  val _: Task[Int] = ZIO.effect(println("hello"))
  // не знаем ни про окружение ни про ошибку

  // любой не падающий эффект
  val _: UIO[Int] = ZIO.effectTotal(println("hello"))

  // From Future
  val f: Future[Int] = ???
  val _: Task[Int] = ZIO.fromFuture(ec => f)

  // From try
  val t: Try[String] = ???
  val _: Task[String] = ZIO.fromTry(t)

  // From either
  val e: Either[String, Int] = ???
  val _: IO[String, Int] = ZIO.fromEither(e)

  // From option
  val opt: Option[Int] = ???
  val z: IO[Option[Nothing], Int] = ZIO.fromOption(opt)
  // type-rotation: move type information into error-channel
  val zz: UIO[Option[Int]] = z.option // if need option from result
  val _: UIO[Option[Nothing]] = zz.some
  zz.sandbox

  // From function  - sore environment dependence
  //  тк это функция появляется зависимость, которая параметризованная типом
  //  String - это шае Environment
  val _: URIO[String, Unit] =
    ZIO.fromFunction[String, Unit](str => println(str.toInt))

  // особые версии конструкторов
  val _: UIO[Unit] = ZIO.unit // такой ZIO падать не должен

  val _: UIO[Option[Nothing]] =
    ZIO.none // зафейлинный зио в коором в качестве ошщибки None

  val _: UIO[Nothing] = ZIO.never // никогда не закончит своего выполнение
  // не может быть сконструирована поэтому Nothing. Analogue while-true cycle w/o CPU-load

  val _: ZIO[Any, Nothing, Nothing] =
    ZIO.die(new Throwable("Died")) // мертвый zio

  val _: ZIO[Any, Int, Nothing] = ZIO.fail(7) // зафейленый зио
}

object zioOperators {

  // for combine effects we mainly use `map` and `flatMap` functions but also addThen, zip, etc...

  /** *
    *
    * 1. Создать ZIO эффект который будет читать строку из консоли
    */

  lazy val readLine: Task[String] = ZIO.effect(StdIn.readLine())

  /** *
    *
    * 2. Создать ZIO эффект который будет писать строку в консоль
    */

  def writeLine(str: String): Task[Unit] = ZIO.effectTotal(println(str))

  /** *
    * 3. Создать ZIO эффект котрый будет трансформировать эффект содержащий строку в эффект содержащий Int
    */
  // toInt может быть эффектом (потенциально является), поэтому заворачиваем в ZIO через flatMap
  lazy val lineToInt: ZIO[Any, Throwable, Int] =
    readLine.flatMap(str => ZIO.effect(str.toInt))

  /** *
    * 3.Создать ZIO эффект, который будет работать как echo для консоли
    *
    */
  //lazy val echo = readlLine.map(str => writeLine(str))
  val echo = readLine.flatMap(str => writeLine(str))

  val echo2 = readLine.flatMap(writeLine)

  val echo3 =
    for {
      str <- readLine
      _ <- writeLine(str)
    } yield ()

  //// from: https://alvinalexander.com/scala/zio-cheatsheet/
  // execute readLine, then pass its result to printLine.
  // flatMap can be read like “and then do Expression2 with
  // the result of Expression1”:
  val echo0 = readLine.flatMap(line => writeLine(line))

  /**
    * Создать ZIO эффект, который будет привествовать пользователя и говорить, что он работает как echo
    */

  // вложенные
  lazy val greetAndEchoZip: ZIO[Any, Throwable, (Unit, Unit)] =
    writeLine("Hello. I am working as echo.").zip(
      readLine.flatMap(echo => writeLine(echo))
    )

  lazy val greetAndEchoflatMap: ZIO[Any, Throwable, Unit] =
    writeLine("Hello. I am working as echo.").flatMap(_ =>
      readLine.flatMap(echo => writeLine(echo))
    )

  lazy val greetAndEchoZipRight: ZIO[Any, Throwable, Unit] =
    writeLine("Hello. I am working as echo.").zipRight(
      readLine.flatMap(echo => writeLine(echo))
    )

  lazy val greetAndEchoZipRightShort: ZIO[Any, Throwable, Unit] =
    writeLine("Hello. I am working as echo.") *>
      (readLine.flatMap(echo => writeLine(echo)))

  // в цепочке
  lazy val greetAndEcho2 = //: ZIO[Any, Throwable, (Unit, Unit)] =
    //writeLine(greet).flatMap(_ => echo)
    writeLine("What is your name? ")
      .flatMap(_ => readLine)
      .flatMap(name => writeLine(s"Hello, $name. I am working as echo. "))
      .flatMap(_ => echo)

  // for-yield
  lazy val greetAndEcho3 =
    for {
      _ <- writeLine("What is your name?")
      name <- readLine
      _ <- writeLine(s"Hello, $name. I am working as echo. ")
      echo <- echo
    } yield (name, echo)

  /**
    * Дпугие версии ZIP
    */

  lazy val a1: Task[Int] = ???
  lazy val b1: Task[String] = ???

  // zip can combine effects returns Tuple of (Int, String):
  lazy val _: ZIO[Any, Throwable, (Int, String)] = a1.zip(b1)

  // но есть кейсы когда одно из значений этого кортеже не нужно, например там Unit:
  // zipRight   *>
  lazy val _: ZIO[Any, Throwable, String] = a1 *> b1

  // zipLeft    *<
  lazy val _: ZIO[Any, Throwable, Int] = a1 <* b1

  // например мы можем комбинировать эффекты вот так:
  lazy val _: ZIO[Any, Throwable, Int] =
    ZIO.effect(println("hello")) *> ZIO.effectTotal(1 + 1)
  // т.е. нам важен только результат он Int

  /*
    Мы собираем эффекты в цепочку для того чтобы они выполнились. Иначе они могут не выполниться. Если их
    не собрать в цепочку тем или иным оператором, их придется выполнять самим по-отдельности

    Эффекты выполня.тся всегда последовательно. Но zipRight, zipLeft - какой результат мы игнорируем
   */

  //  writeLine("Hello. What is your name?").flatMap(_ => we can make it simplify
  // greet and echo улучшенный
  lazy val betterGreet =
    writeLine("Hello. What is your name?") *>
      readLine.flatMap(name => writeLine(s"Hello, $name!"))

  lazy val betterEcho: ZIO[Any, Throwable, Unit] =
    writeLine("I am echo. Enter some one.") *>
      readLine.flatMap(s => writeLine(s))

  /**
    * Используя уже созданные эффекты, написать программу, которая будет считывать поочереди считывать две
    * строки из консоли, преобразовывать их в числа, а затем складывать их
    */

  lazy val r1: ZIO[Any, Throwable, Int] =
    readLine.flatMap(s1 =>
      readLine.flatMap(s2 => ZIO.effect(s1.toInt + s2.toInt))
    )

  /**
    * Второй вариант
    */

  lazy val r2 = for {
    s1 <- lineToInt
    s2 <- lineToInt
    //  _ <- writeLine(s1 + s2 + "")
    //} yield ()
  } yield (s1 + s2)

  /**
    * Доработать написанную программу, чтобы она еще печатала результат вычисления в консоль
    */

  // how to write to screen:
  // variant 1
  lazy val r3 = r2.flatMap(n => writeLine(n.toString))

  // variant 2
  lazy val r4 = for {
    res <- r2
    _ <- writeLine(res.toString)
  } yield ()

  //
  lazy val a: Task[Int] = ZIO.effect(42)
  lazy val b: Task[String] = ZIO.effect("I am string")

  /**
    * последовательная комбинация эффектов a и b
    */
  lazy val ab1: ZIO[Any, Throwable, (Int, String)] =
    a.zip(b)

  /**
    * последовательная комбинация эффектов a и b
    */
  lazy val ab2: ZIO[Any, Throwable, Int] = {
    a.zipLeft(b) // as <*
  }

  /**
    * последовательная комбинация эффектов a и b
    */
  lazy val ab3: ZIO[Any, Throwable, String] = {
    a *> b
  }

  /**
    * Последовательная комбинация эффета b и b, при этом результатом должна быть конкатенация
    * возвращаемых значений
    */
  // zip effect with another effect using combiner function
  lazy val ab4: ZIO[Any, Throwable, String] =
    b.zipWith(b)((x, y) => x + y) // zipWith, more shorter: b.zipWith(b)(_+_)

  // allow us represent effect with one type as effect with another type
  //  maps of the success value from previous effect to the specified value
  lazy val c: ZIO[Clock, Throwable, Int] = ZIO.effect("").as(7) //as

  def readFile(fileName: String): ZIO[Any, IOException, String] = ???

  lazy val _: URIO[Any, String] = readFile("test.txt").orDie

}

object zioRecursion {

  /** **
    * Написать программу, которая считывает из консоли Int введнный пользователем,
    * а в случае ошибки, сообщает о некорректном вводе, и просит ввести заново
    *
    */

  lazy val readInt: ZIO[Console, Throwable, Int] =
    readLine.flatMap(str => ZIO.effect(str.toInt))
  /* if we write like
    readLine.map(str => str.toInt) - this unwrap in effect and orElse next not worked
   */

  // orElse - в случае неуспешного выполнения первого эффекта, позволяет выполнить другой эффект
  //  а в случае успешного выполняет сам эффект и все.
  //  *> - zipRight
  lazy val readIntOrRetry: ZIO[Console, Throwable, Int] =
    readInt.orElse(
      // combine two effects into single effect
      ZIO.effect(println("Error, repeat input please!")) *> (readIntOrRetry)
    )

  lazy val tt = readInt.option

  /**
    * Считаем факториал
    */
  def factorial(n: Int): Int =
    if (n <= 1) n
    else n * factorial(n - 1)

  /**
    * Написать ZIO версию ф-ции факториала
    * zip recursion optimised and not stackoverflow
    */
  def factorialZ(n: BigDecimal): Task[BigDecimal] = {
    if (n <= 1) ZIO.succeed(n)
    // combine `effect of success` AND `effect on (n-1)` using `multiple *`
    else ZIO.succeed(n).zipWith(factorialZ(n - 1))(_ * _)

  }

}
