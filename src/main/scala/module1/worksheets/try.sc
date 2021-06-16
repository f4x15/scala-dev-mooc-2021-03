import scala.util.{Try,Success,Failure}

def toInt(s: String): Try[Int] = Try {
  // if w/o exception => to Success. If with exception => Failure
  Integer.parseInt(s.trim)
}

