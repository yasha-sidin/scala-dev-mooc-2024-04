package ru.otus.module2

import scala.language.implicitConversions

object homework_hkt_implicits {

  trait Bindable[F[_], A] {
    def map[B](f: A => B): F[B]

    def flatMap[B](f: A => F[B]): F[B]
  }

  object Bindable {
    implicit def listBindable[A](itr: List[A]): Bindable[List, A] = new Bindable[List, A] {
      override def map[B](f: A => B): List[B] = itr.map(f)
      override def flatMap[B](f: A => List[B]): List[B] = itr.flatMap(f)
    }

    implicit def optionBindable[A](opt: Option[A]): Bindable[Option, A] = new Bindable[Option, A] {
      override def map[B](f: A => B): Option[B] = opt.map(f)
      override def flatMap[B](f: A => Option[B]): Option[B] = opt.flatMap(f)
    }
  }

  implicit class BindableSyntax[F[_], A](val ev: F[A]) {
    def toBindable(implicit bindableConverterA: F[A] => Bindable[F, A]): Bindable[F, A] = bindableConverterA(ev)
  }

  def tupleF[F[_], A, B](fa: F[A], fb: F[B])(implicit bindableConverterA: F[A] => Bindable[F, A],
                                             bindableConverterB: F[B] => Bindable[F, B]): F[(A, B)] = {
    fa.toBindable.flatMap(a => fb.toBindable.map(b => (a, b)))
  }

  def main(args: Array[String]): Unit = {
    println(tupleF(List(1, 2, 3), List("str1", "str2", "str3")))
    println(tupleF(Option(1), Option("str1")))
  }
}