package ru.otus.module1

import scala.annotation.tailrec
import scala.language.postfixOps


/**
 * referential transparency
 */


// recursion

object recursion {

  /**
   * Реализовать метод вычисления n!
   * n! = 1 * 2 * ... n
   */

  def fact(n: Int): Int = {
    var _n = 1
    var i = 2
    while (i <= n) {
      _n *= i
      i += 1
    }
    _n
  }


  def factRec(n: Int): Int =
    if (n <= 0) 1 else n * factRec(n - 1)

  def factTailRec(n: Int): Int = {

    def go(n: Int, accum: Int): Int =
      if (n <= 0) accum else go(n - 1, n * accum)

    go(n, 1)
  }

  /**
   * реализовать вычисление N числа Фибоначчи
   * F0 = 0, F1 = 1, Fn = Fn-1 + Fn - 2
   */


}


object hof {

  // обертки

  def logRunningTime[A, B](f: A => B): A => B = a => {
    val start = System.currentTimeMillis()
    val result: B = f(a)
    val end = System.currentTimeMillis()
    println(s"Running time: ${end - start}")
    result
  }

  def doomy(string: String): Unit = {
    Thread.sleep(1000)
    println(string)
  }

  // изменение поведения ф-ции

  def not[A](f: A => Boolean): A => Boolean = a => !f(a)

  def isOdd(i: Int): Boolean = i % 2 > 0

  val isEven: Int => Boolean = not(isOdd)



  // изменение самой функции

  def partial[A, B, C](a: A, f: (A, B) => C): B => C = b => f(a, b)

  def partial2[A, B, C](a: A, f: (A, B) => C): B => C =
    f.curried(a)

  def sum(x: Int, y: Int): Int = x + y

  val p: Int => Int = partial(3, sum)
  p(2) // 5
  p(3) // 6
  partial(3, sum)(3) // 6


}


/**
 * Реализуем тип Option
 */


object opt {


  class Animal

  class Dog extends Animal

  /**
   *
   * Реализовать структуру данных Option, который будет указывать на присутствие либо отсутсвие результата
   */
  // + covariance
  // - contravariance

  sealed trait Option[+T] {
    def isEmpty: Boolean = this match {
      case None => true
      case Some(v) => false
    }

    def get: T = this match {
      case None => throw new Exception("get ob empty option")
      case Some(v) => v
    }

    def map[B](f: T => B): Option[B] = flatMap(t => Option(f(t)))

    def flatMap[B](f: T => Option[B]): Option[B] = this match {
      case Some(v) => f(v)
      case None => None
    }

    def printIfAny(): Unit = this match {
      case Some(v) => println(v)
      case _ =>
    }

    def zip[B](b: Option[B]): Option[(T, B)] = this match {
      case Some(v) => b match {
        case Some(v1) => opt.Option((this.get, b.get))
        case _ => None
      }
      case _ => None
    }

    def filter(f: T => Boolean): Option[T] = this match {
      case Some(v) => if (f(v)) this else None
      case _ => None
    }
  }


  val opt1: Option[Int] = ???
  val opt2: Option[Int] = opt1.map(i => i + 1)

  case class Some[T](v: T) extends Option[T]

  case object None extends Option[Nothing]


  object Option {
    def apply[T](v: T): Option[T] =
      if (v != null) Some(v) else None
  }


  /**
   *
   * Реализовать метод printIfAny, который будет печатать значение, если оно есть
   */


  /**
   *
   * Реализовать метод zip, который будет создавать Option от пары значений из 2-х Option
   */


  /**
   *
   * Реализовать метод filter, который будет возвращать не пустой Option
   * в случае если исходный не пуст и предикат от значения = true
   */

}

object list {
  /**
   *
   * Реализовать односвязанный иммутабельный список List
   * Список имеет два случая:
   * Nil - пустой список
   * Cons - непустой, содержит первый элемент (голову) и хвост (оставшийся список)
   */

  sealed trait List[+T] {


    def ::[TT >: T](el: TT): List[TT] = new::(el, this)

    def :::[TT >: T](list: List[TT]): List[TT] = list match {
      case ::(h, Nil) => h :: this ::: Nil
      case ::(h, t) => h :: t ::: this
      case Nil => this
    }

    override def toString: String = {
      "List(" + this.mkString(", ") + ")"
    }

    def mkString(s: String): String = this match {
      case Nil => ""
      case ::(h, Nil) => h + ""
      case ::(h, t) => "" + h + s + t.mkString(s)
    }

    def reverse: List[T] = {
      @tailrec
      def getLast(list: List[T]): List[T] = list match {
        case ::(h, Nil) => h :: Nil
        case ::(h, t) => getLast(t)
        case _ => Nil
      }

      def getListWithEmptyLast(list: List[T]): List[T] = list match {
        case ::(h, Nil) => Nil
        case ::(h, t) => h :: getListWithEmptyLast(t)
        case _ => Nil
      }

      this match {
        case ::(h, t) => getLast(this) ::: getListWithEmptyLast(this).reverse
        case _ => Nil
      }
    }

    def map[B](f: T => B): List[B] = this match {
      case ::(h, t) => new::(f(h), t.map(f))
      case _ => Nil
    }

    def filter(p: T => Boolean): List[T] = this match {
      case ::(h, t) => (if (p(h)) new::(h, Nil) else Nil) ::: t.filter(p)
      case Nil => Nil
    }
  }

  case class ::[T](head: T, tail: List[T]) extends List[T]
  case object Nil extends List[Nothing]

  object List {
    def apply[A](v: A*): List[A] = if (v.isEmpty) Nil
    else ::(v.head, apply(v.tail: _*))
  }

  1 :: 2 :: 3 :: Nil

  /**
   * Конструктор, позволяющий создать список из N - го числа аргументов
   * Для этого можно воспользоваться *
   *
   * Например вот этот метод принимает некую последовательность аргументов с типом Int и выводит их на печать
   * def printArgs(args: Int*) = args.foreach(println(_))
   */

  /**
   *
   * Реализовать метод reverse который позволит заменить порядок элементов в списке на противоположный
   */

  /**
   *
   * Реализовать метод map для списка который будет применять некую ф-цию к элементам данного списка
   */


  /**
   *
   * Реализовать метод filter для списка который будет фильтровать список по некому условию
   */

  /**
   *
   * Написать функцию incList котрая будет принимать список Int и возвращать список,
   * где каждый элемент будет увеличен на 1
   */


  /**
   *
   * Написать функцию shoutString котрая будет принимать список String и возвращать список,
   * где к каждому элементу будет добавлен префикс в виде '!'
   */

}