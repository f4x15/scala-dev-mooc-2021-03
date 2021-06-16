package module1

import module2.implicits.{implicit_conversions, implicit_scopes}
import module2.{higher_kinded_types, implicits, type_classes}
import module3.functional_effects.functionalProgram.{
  declarativeEncoding,
  executableEncoding
}
import module3.tryFinally.zioBracket
import module3.zioOperators.writeLine
import module3.{zioConcurrency, zioOperators, zioRecursion, zioZManaged}
import zio.Cause.{Both, Fail, Internal}
import zio.console.{Console, getStrLn, putStrLn}
import zio.duration.durationInt
import zio.{Fiber, IO, Task, UIO, URIO, ZIO}

import scala.concurrent.Future
import scala.language.{existentials, implicitConversions, postfixOps}
import scala.util.Try

//object App {

object App { // extends zio.App {

  def main(args: Array[String]): Unit = {

    object cuncerrent_multipleZioError {
      // рассмотрим конкурентные ошибки:
      // пусть есть такая же модель ошибок:
      sealed trait NotificationError

      case object NotificationByEmailFailed extends NotificationError

      case object NotificationBySMSFailed extends NotificationError

      // and two effects, that fails with:
      val z1 = ZIO.fail(NotificationByEmailFailed)
      val z2 = ZIO.fail(NotificationBySMSFailed)

      // and combine this with concurrency: zipPar
      // tapCause - достучаться до полного представления ошибки
      // и заматчимся на кейсе-case
      val app = z1
        .zipPar(z2)
        .tapCause {
          // две ошибки для двух эффектов
          case Both(c1, c2) =>
            // соеденим вывод одной ошиибки с выводом другой ошибки
            putStrLn(c1.failureOption.toString) *> // zipRight
              putStrLn(c2.prettyPrint) // .toString for short
          //  if `c2.prettyPrint` than we see a long-sheet-of-error
          //  ii `c2.toString` see кишки какие объекты zio хранит там
        }
        .orElse(putStrLn("app is fail")) // for other errors for short
      // в итоге есть некоторый app-effect который представляет результат выполнения
      //  конкурентного эффекта z1 и z2, и дальше пытаемся залогировать обе ошибки
      //  с1 - error for z1, c2 - error for z2
      //  tapCause - стучимся до расширенной модели ошибок и залогировать что произошло
      //  НУ и в целом у нас есть что если зафейлиться общий эффект `val app` мы ведемем
      //  что `app is fail`, app падает с check-error
      /* output:
      Some(NotificationByEmailFailed)   // ошибку с которой зафейлился 1й эффект
      Some(NotificationBySMSFailed)     // ошибку с которой зафейлился 2й эффект
      app is fail                       // ошибку с которой зафейлился эффект app

      Прикол весь в том: что у нас ошибки сами вкладываются друг-в-дружку, у нас будет
      комбинация ошибок, а не последняя как было бы в случае с Future.
      Here `Both` - имеет всю структуру, те будет сколь-угодно большая вложенность ошибок


       */

    }

    zio.Runtime.default.unsafeRun(cuncerrent_multipleZioError.app)

    /*
    zio.Runtime.default.unsafeRun(
      zioRecursion.factorialZ(100).flatMap(res => writeLine(res.toString()))
    )
     */
    // Thread.sleep(5000)
  }

  /*  // run effectfull applications
  def run(args: List[String]) =
    myAppLogic.exitCode

  import module3.zioOperators._

  val myAppLogic =
    for {
      res <- ab4
      _ <- writeLine(res)
    } yield ()
   */
}
