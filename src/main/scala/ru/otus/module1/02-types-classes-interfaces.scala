package ru.otus.module1

import java.io.{Closeable, File}
import scala.io.{BufferedSource, Source}
import scala.util.{Try, Using}



object type_system {

  /**
   * Scala type system
   *
   */



  def absurd(v: Nothing) = ???


  // Generics


  val file: File = new File("ints.txt")
  val source: BufferedSource = Source.fromFile(file)
//  val lines: List[String] = try{
//    source.getLines().toList
//  } finally {
//    source.close()
//  }

  def ensureClose[S, R](source: S)(release: S => Any)(f: S => R): R = {
    try{
      f(source)
    } finally {
      release(source)
    }
  }

//  val r1: List[String] = ensureClose(source)(_.close())(s => s.getLines().toList)
//  val r2: String = ensureClose(source)(_.close()){s =>
//    s.mkString(",")
//    s.mkString(",")
//    s.mkString(",")
//  }














  // ограничения связанные с дженериками


  /**
   *
   * class
   *
   * конструкторы / поля / методы / компаньоны
   */


  class User private (val email: String, val password: String){
    def getEmail: String = email
    def getPassword: String = password

  }


  object User{
    def apply(email: String = "email@gmail.com", password: String = "12345"): User =
      new User(email, password)
    def from(password: String): User =
      new User( "email@gmail.com", password)

  }

  val user: User = User.apply(password = "56789")






  /**
   * Задание 1: Создать класс "Прямоугольник"(Rectangle),
   * мы должны иметь возможность создавать прямоугольник с заданной
   * длиной(length) и шириной(width), а также вычислять его периметр и площадь
   *
   */


  /**
   * object
   *
   * 1. Паттерн одиночка
   * 2. Ленивая инициализация
   * 3. Могут быть компаньоны
   */


  /**
   * case class
   *
   */



    // создать case класс кредитная карта с двумя полями номер и cvc


  case class CreditCard(number: String, cvc: Int)

  case object Cash

  val cc: CreditCard = CreditCard("3234234", 123)
  val cc3: CreditCard = CreditCard("3234234", 123)

  // cc == cc3 // true

  val cc2 = cc.copy(cvc = 567)



  /**
   * case object
   *
   * Используются для создания перечислений или же в качестве сообщений для Акторов
   */



  /**
   * trait
   *
   */


  sealed trait UserService{
    def get(id: Int): User
    def insert(u: User): Unit
    def foo: Int
  }

  trait Identifiable{
    def id: Int
  }

  class UserServiceImpl extends UserService{
    override def get(id: Int): User = ???

    override def insert(u: User): Unit = ???

    override def foo: Int = 10
  }

  val us = new UserServiceImpl with Identifiable{
    override def id: Int = 0
  }

  val us2 = new UserService {
    override def get(id: Int): User = ???

    override def insert(u: User): Unit = ???

    override def foo: Int = ???
  }















  class A {
    def foo() = "A"
  }

  trait B extends A {
    override def foo() = "B" + super.foo()
  }

  trait C extends B {
    override def foo() = "C" + super.foo()
  }

  trait D extends A {
    override def foo() = "D" + super.foo()
  }

  trait E extends C {
    override def foo(): String = "E" + super.foo()
  }



  // CBDA
  // A -> D -> B -> C
  // CBDA
  val v = new A with D with C with B


  // A -> B -> C -> E -> D
  // DECBA == DECBA
  val v1 = new A with E with D with C with B


  /**
   * Value classes и Universal traits
   */


}