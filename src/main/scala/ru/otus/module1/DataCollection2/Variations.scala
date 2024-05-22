package ru.otus.module1.DataCollection2

class Variations {
  trait Fruit
  class Apple extends  Fruit

  class Box[+T](item: T)
  val fruitBox: Box[Fruit] = new Box[Apple](new Apple)


  class Printer[-T] {
    def print(item: T): Unit = println(item)
  }

  val stringPrinter: Printer[String] = new Printer[Any]
  stringPrinter.print("Hello")

  class Stack[T](items: List[T]) {
    def push(item: T): Stack[T] = new Stack(item :: items)
  }

  val intStack: Stack[Int] = new Stack(List(1, 2, 3))
  val stringStack: Stack[String] = new Stack(List("a", "b", "c"))

}
