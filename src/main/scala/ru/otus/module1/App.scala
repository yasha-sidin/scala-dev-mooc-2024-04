package ru.otus.module1

object App {
  def main(args: Array[String]): Unit = {
    /**
     * Test Option
     */
    val optTest = opt.Option(5)
    val optTestNone = opt.None
    // printIfAny
    optTest.printIfAny()
    optTestNone.printIfAny()
    // zip
    println(optTest.zip(opt.None))
    println(optTestNone.zip(optTest))
    println(optTest.zip(opt.Option(12)))
    // filter
    println(optTest.filter(_ > 4))
    println(optTest.filter(_ > 5))
    println(optTestNone.filter(x => true))

    /**
     * Test List
     */
    val testList = list.List(1, 3, 42, 4)
    println(testList.mkString(" - "))
    println(1 :: 233 :: 3213 :: 333 :: Nil)
    println(testList)
    println(Nil)
    println(testList.map(_ + 100))
    println(testList.reverse)
    println(testList ::: list.List(1, 34, 5555))
    println((testList ::: list.List(1, 34, 5555)).reverse)
    println(testList.filter(_ > 3))

    def incList(list: List[Int]): List[Int] = list.map(_ + 1)

    println(incList(List(1, 1, 2, 2, 3, 3)))

    def shoutString(list: List[String]): List[String] = list.map("!" + _)

    println(shoutString(List("hello", "world")))

  }
}
