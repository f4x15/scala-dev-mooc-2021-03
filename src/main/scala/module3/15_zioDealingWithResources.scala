package module3

import module3.toyZManaged.ZManaged
import module3.tryFinally.traditional.Resource
import zio.{IO, RIO, Task, UIO, URIO, ZIO}
import zio.console.{Console, putStrLn}

import java.io.{Closeable, IOException}
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success}
import scala.language.postfixOps

object tryFinally {

  object traditional {

    trait Resource

    lazy val acquireResource: Resource = ???

    def use(resource: Resource): Unit = ???

    def releaseResource(resource: Resource): Unit = ???

    /**
      * Напишите код, который обеспечит корректную работу с ресурсом:
      * получить ресурс -> использовать -> освободить
      *
      */

    lazy val result1 = {
      val res = acquireResource
      try {
        use(res)
      } finally {
        releaseResource(res)
      }
    }

    def withResource[R <: Closeable](resource: => R)(use: R => Any): Unit = {
      try {
        use(resource)
      } finally {
        resource.close()
      }
    }

  }

  object future {
    implicit val global = scala.concurrent.ExecutionContext.global

    lazy val acquireFutureResource: Future[Resource] = ???

    def use(resource: Resource): Future[Unit] = ???

    def releaseResource(resource: Resource): Future[Unit] = ???

    /**
      * Написать вспомогательный оператор, котрый позволит корректно работать
      * с ресурсами в контексте Future
      *
      */

    def ensuring[A](f: Future[A])(release: Future[Any]): Future[A] =
      f.transformWith {
        case Failure(exception) =>
          release.flatMap(_ => Future.failed(exception))
        case Success(value) => release.flatMap(_ => Future.successful(value))
      }

    /**
      * Написать код, который получит ресурс, воспользуется им и освободит
      */
    lazy val result2Future = ???

    val _ = acquireFutureResource.flatMap { r =>
      ensuring(use(r))(releaseResource(r))
    }

  }

  object zioBracket {

    trait File {
      def name: String
      def close: Unit = println(s"File -${name}- closed")
      def readLines: List[String] = List("Hello world", "Scala is cool")
    }

    object File {
      def apply(_name: String): File =
        new File {
          override def name: String = _name
        }

      def apply(_name: String, lines: List[String]): File =
        new File {
          override def name: String = _name
          override def readLines: List[String] = lines
        }
    }

    /**
      * реалтзовать ф-цию, которая будет описывать открытие файла с помощью ZIO эффекта
      */

    def openFile(fileName: String): IO[IOException, File] =
      ZIO.fromEither(Right(File(fileName)))

    def openFile(fileName: String, lines: List[String]): IO[IOException, File] =
      ZIO.fromEither(Right(File(fileName, lines)))

    /**
      * реалтзовать ф-цию, которая будет описывать закрытие файла с помощью ZIO эффекта
      */

    def closeFile(file: File): UIO[Unit] = URIO(file.close)

    /**
      * Написать эффект, котрый прочитает строчки из файла и выведет их в консоль
      */

    def handleFile(file: File): ZIO[Console, Nothing, List[Unit]] = {
      val lines: List[String] = file.readLines
      ZIO.foreach(lines)(putStrLn(_))
    }

    /**
      * Написать эффект, который откроет 2 файла, прочитает из них строчки,
      * выведет их в консоль и корректно закроет оба файла
      */

    val twoFiles: ZIO[Console, IOException, List[Unit]] =
      ZIO.bracket(openFile("test1"))(closeFile) { f1 =>
        ZIO.bracket(openFile("test1"))(closeFile) { f2 =>
          handleFile(f1) *> handleFile(f2)
        }
      }

    /**
      * Рефакторинг выше написанного кода
      *
      */

    def withFile[R, A](
        name: String
    )(use: File => RIO[R, A]): ZIO[R, Throwable, A] = {
      openFile(name).bracket(closeFile)(use)
    }

    val twoFiles2: ZIO[Console, Throwable, List[Unit]] =
      withFile("test1") { f1 =>
        withFile("test2") { f2 =>
          handleFile(f1) *> handleFile(f2)
        }
      }

  }

}

object toyZManaged {

  import module3.tryFinally.zioBracket._

  final case class ZManaged[-R, +E, A](
      acquire: ZIO[R, E, A],
      release: A => URIO[R, Any]
  ) { self =>

    def use[R1 <: R, E1 >: E, B](f: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      acquire.bracket(release)(f)

    def map[B](f: A => B): ZManaged[R, E, B] = ???

    def flatMap[R1 <: R, E1 >: E, B](
        f: A => ZManaged[R1, E1, B]
    ): ZManaged[R1, E1, B] = ???

    def zip[R1 <: R, E1 >: E, B](
        that: ZManaged[R1, E1, B]
    ): ZManaged[R1, E1, (A, B)] = ???

  }

}

object zioZManaged {

  import zio.ZManaged
  import module3.tryFinally.zioBracket._

  /**
    * Создание ZManaged
    */

  /**
    * написать эффект открывающий / закрывающий первый файл
    */
  val file1: ZManaged[Any, IOException, File] =
    ZManaged.make(openFile("test1"))(closeFile)

  /**
    * написать эффект открывающий / закрывающий второй файл
    */
  val file2: ZManaged[Any, IOException, File] =
    ZManaged.make(openFile("test2"))(closeFile)

  /**
    * Использование ресурсов
    */

  /**
    * Написать эффект, котрый восользуется ф-цией handleFile из блока про bracket
    * для печати строчек в консоль
    */
  val printFile1 = file1.use(handleFile)

  /**
    * Комбинирование ресурсов
    */

  // Комбинирование
  lazy val combined: ZManaged[Any, IOException, (File, File)] = file1 zip file2

  // Паралельное открытие / закрытие
  lazy val combined2: ZManaged[Any, IOException, (File, File)] =
    file1 zipPar file2

  /**
    * Написать эффект, который прочитает и выведет строчки из обоих файлов
    */
  val combinedEffect = combined.use {
    case (f1, f2) =>
      handleFile(f1) *> handleFile(f2)
  }

  /**
    * Множество ресурсов
    */

  lazy val fileNames: List[String] = ???

  def file(name: String): ZManaged[Any, IOException, File] = ???

  // множественное открытие / закрытие
  lazy val files: ZManaged[Any, IOException, List[File]] =
    ZManaged.foreach(fileNames)(file)

  // паралельное множественное открытие / закрытие
  lazy val files2: ZManaged[Any, IOException, List[File]] =
    ZManaged.foreachPar(fileNames)(file)

  // Использование

  def processFiles(file: File*): Task[Unit] = ???

  // обработать N файлов
  lazy val r1: ZIO[Any, Throwable, Unit] = files.use(l => processFiles(l: _*))

  lazy val r2: ZIO[Any, Throwable, Unit] = ???

  lazy val files3: ZManaged[Any, IOException, List[File]] = ???

  /**
    * Прочитать строчки из файлов и вернуть список этих строк используя files3
    */
  lazy val r3: Task[List[String]] = files3.use { files =>
    Task
      .foreach(files) { file =>
        Task(file.readLines)
      }
      .map(_.flatten)
  }

  /**
    * Эффект описывающий чтение списка файлов с заданным паралелизмом
    */
  lazy val filesN: ZManaged[Any, Throwable, List[File]] = ???

  // Конструирование

  lazy val eff1: Task[Int] = ???

  // Из эффекта
  lazy val m1: ZManaged[Any, Throwable, Int] = ZManaged.fromEffect(eff1)
  def mkTransactor(c: Config): ZManaged[Any, Throwable, Int] = ???

  // микс ZManaged и ZIO
  type Config
  val config: Task[Config] = ???

  lazy val m2: ZManaged[Console, Throwable, Unit] = for {
    conf <- config.toManaged_
    _ <- mkTransactor(conf)
  } yield ???

  //Еще несколько вариантов использования
  val _: ZIO[Console, Throwable, Unit] = m2.useNow

  val _: ZIO[Console, IOException, Unit] = m2.use_(putStrLn("Ooops"))

}
