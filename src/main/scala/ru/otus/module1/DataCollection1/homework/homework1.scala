package ru.otus.module1.DataCollection1.homework

import scala.annotation.tailrec
import scala.util.Random

class BallsExperiment {
  private val listOfBalls: List[Int] = List(1, 0, 1, 0, 1, 0)

  private def getTupleBalls: (Int, Int) = {
    def handleBall(list: List[Int]): (Int, List[Int]) = {
      val shuffleList = Random.shuffle(list)
      val ball = shuffleList.head
      val otherBalls = shuffleList.tail
      (ball, otherBalls)
    }
    val firstHandle = handleBall(listOfBalls)
    val firstBall = firstHandle._1
    val remainingBalls = firstHandle._2
    val secondBall = handleBall(remainingBalls)._1
    (firstBall, secondBall)
  }

  def isFirstBlackSecondWhite: Boolean = {
    /*
    Вероятность появления белого шара хотя бы один раз за два вынимания
     */
    getTupleBalls match {
      case (_, 1) => true
      case (1, _) => true
      case _ => false
    }
  }
}

object BallsExperiment {
  def apply(): BallsExperiment = new BallsExperiment
}

object BallsTest {
  def fillListOfExperiments(count: Int): List[BallsExperiment] = {
    @tailrec
    def helperDef(count: Int, list: List[BallsExperiment]): List[BallsExperiment] = {
      count match {
        case x if x <= 0 => list
        case _ => helperDef(count - 1, BallsExperiment() :: list)
      }
    }
    helperDef(count, Nil)
  }
  def main(args: Array[String]): Unit = {
    val count = 100000
    val listOfExperiments: List[BallsExperiment] = fillListOfExperiments(count)
    val countOfExperiments = listOfExperiments.map(_.isFirstBlackSecondWhite)
    val countOfPositiveExperiments: Float = countOfExperiments.count(_ == true)
    println(countOfPositiveExperiments / count)
  }
}