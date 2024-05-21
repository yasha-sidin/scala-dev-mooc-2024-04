package ru.otus.module1.DataCollection1

object ForComprehansion {
  def main(args: Array[String]): Unit = {
    val RGB = Seq("R", "G", "B")
    val range = Range(1,4)
    val map = Map("R"->"Red", "G"-> "Green", "B"-> "Blue")

    for (el <- RGB)
      println(el)

    for (el <- RGB; el1 <- range) {
      println(s"$el $el1")
    }

    for ((key, value) <- map)
      println(s"$key $value")

    println("--------------------------------")
    for (
      el1 <- RGB;
      el2 <- RGB;
      el3 <- RGB
    ) {
      println(s"$el1 $el2 $el3")
    }

    println("make it uniq")
    for (
      el1 <- RGB;
      el2 <- RGB;
      el3 <- RGB;
      if el1 != el2;
      if el3 != el2 && el3 != el1
    ) {
      println(s"$el1 $el2 $el3")
    }

    println("next example")
    case class Car(marke: String, model: String, year: Int)
    val cars = Car("VW", "Passat", 2005) :: Car("Lexus", "UX", 2019) :: Car("BMW", "i3", 2021) :: Nil
    case class Garage(name: String, index: Int)
    val garages = Garage("BMW", 1) :: Garage("Lexus", 2) :: Nil

    garages.flatMap{
      garage =>
        cars.filter(car => car.marke == garage.name).map(car => (car.marke, car.model))
    }.foreach(x=> println(s"${x._1} ${x._2}"))

    println("for comp.")
    val cars2000 = for {
      car <- cars if car.year > 2010

    } yield (car.marke, car.model)

    cars2000.foreach(x=>println(s"${x._1} ${x._2}"))

    println("-------------------------------")
    val cars1 = for {
      car <- cars
      garage <- garages
      if car.marke == garage.name
    } yield {
      (car.marke, car.model, garage.index)
    }

    cars1.foreach({
      case (marke, model, index) => Predef.println(StringContext.apply("", " ", " ", "").s(marke, model, index))
    })



  }
}