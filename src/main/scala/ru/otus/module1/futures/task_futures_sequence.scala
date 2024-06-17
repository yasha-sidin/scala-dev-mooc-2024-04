package ru.otus.module1.futures


import ru.otus.module1.futures.HomeworksUtils.TaskSyntax

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object task_futures_sequence {

  /**
   * В данном задании Вам предлагается реализовать функцию fullSequence,
   * похожую на Future.sequence, но в отличии от нее,
   * возвращающую все успешные и не успешные результаты.
   * Возвращаемое тип функции - кортеж из двух списков,
   * в левом хранятся результаты успешных выполнений,
   * в правово результаты неуспешных выполнений.
   * Не допускается использование методов объекта Await и мутабельных переменных var
   */
  /**
   * @param futures список асинхронных задач
   * @return асинхронную задачу с кортежом из двух списков
   */
  def fullSequence[A](futures: List[Future[A]])
                     (implicit ex: ExecutionContext): Future[(List[A], List[Throwable])] = {
    @tailrec
    def getTupleOfResultOptions(f: Future[A]): (Option[A], Option[Throwable]) = if(f.isCompleted) {
      f.value match {
        case Some(Success(result)) => (Some(result), None)
        case Some(Failure(ex)) => (None, Some(ex))
        case None => (None, None)
      }
    } else {
      getTupleOfResultOptions(f)
    }

    def createTuple2WithResultsAndErrors(list: List[(Option[A], Option[Throwable])]): (List[A], List[Throwable]) = {
      val listSuccess = list.map(_._1).filter(_.isDefined).map(_.get)
      val listFailed = list.map(_._2).filter(_.isDefined).map(_.get)
      (listSuccess, listFailed)
    }

    Future(createTuple2WithResultsAndErrors(futures.map(getTupleOfResultOptions)))
  }
}
