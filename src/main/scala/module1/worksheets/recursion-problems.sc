import scala.annotation.tailrec

/**
 * Реализовать метод вычисления n!
 * n! = 1 * 2 * ... n
 */
def fact1(n: Int): Int = {
  var i = 1
  var res = 1

  while (i <= n) {
    res *= i
    i += 1
  }

  res
}

fact1(5)

def fact2(n: Int): Int = {
  if (n <= 1) 1
  else n*fact2(n-1)
}

fact2(5)

// tailrec
def fact3(n: Int): Int = {
  @tailrec
  def go(n: Int, acc: Int): Int = {
    if (n <= 1) acc
    else go(n-1, acc*n)
  }

  go(n, 1)
}

fact3(5)

/**
 * реализовать вычисление N числа Фибоначчи
 * F0 = 0, F1 = 1, Fn = Fn-1 + Fn - 2
 *
 * 0 1 1 2 3 5 8 13
 */
def fib1(n: Int): Int = {
  if (n == 1) 0
  else if (n == 2) 1
  else fib1(n -1) + fib1(n - 2)
}

fib1(8)

def fib2(n: Int): Int = {
  if (n == 1) 0
  else if (n == 2) 1

  var i = 3
  var res = 1
  while(i < n) {
    res += (i-1) + (i-2)
    i = res
  }

  res
}

fib2(3)

// using tail recursion
def fib3(n: Int): Int = {
???
}