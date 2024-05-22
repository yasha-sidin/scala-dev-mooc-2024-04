package ru.otus.module1.DataCollection2

object MonadaRules {

  def sqrt(x:Int): Option[Int] = Some(x*x)
  def inc(x: Int): Option[Int] = Some(x+x)

  //1. left unit law
  //monad.apply(x).flatMap(f) == f(x)
  def leftUnitLaw(): Unit = {
    val x = 5
    val monad: Option[Int] = Some(x)
    val result = monad.flatMap(sqrt) == sqrt(x)
    println(result)
  }

  //2.right unit law
  // monad(x).flatMap.unit == monad
  def rightUnitLaw(): Unit = {
    val x= 5
    val monad:Option[Int] = Some(x)
    val result = monad.flatMap(x=>Some(x)) == monad
    println(result)
  }

  //3 associative
  // (monad flatmap f) flatmap g == monad flatmap(x=> f flatmap g)
  def associativeLaw(): Unit = {
    val x = 5
    val monad:Option[Int] = Some(x)

    val left = monad flatMap sqrt flatMap inc
    val right = monad flatMap(x => sqrt(x) flatMap inc)
    assert(left == right)
  }

  def main(args: Array[String]): Unit = {
    leftUnitLaw
    rightUnitLaw
    associativeLaw
  }


}
