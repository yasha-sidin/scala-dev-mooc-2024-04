package ru.otus.module1.DataCollection2

object Monada {
  class Lazy[+A](v: => A) {
    private lazy val internal = v

    def flatMap[B](f: (=> A) => Lazy[B]): Lazy[B] = f(internal)

    def map[B](f: A => B): Lazy[B] = ???
  }

  object Lazy {
    def apply[A](v: => A): Lazy[A] = new Lazy(v)
  }

  def main(args: Array[String]): Unit = {
    val res = for {
      el1 <- Lazy(1)
      el2 <- Lazy(2)
    } yield ()

  }


}
