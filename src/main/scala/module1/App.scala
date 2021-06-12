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

    zio.Runtime.default.unsafeRun(
      zioRecursion.factorialZ(100).flatMap(res => writeLine(res.toString()))
    )

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
