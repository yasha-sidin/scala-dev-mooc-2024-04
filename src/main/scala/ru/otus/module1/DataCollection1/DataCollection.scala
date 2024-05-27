package ru.otus.module1.DataCollection1

object DataCollection {
  def main(args: Array[String]): Unit = {
    val demoCollectionList1 = "line 1" :: "line 2" :: "line 3" :: Nil
    val demoCollectionList2 = List("line 1", "line 2", "line 3")

    val demoCollectionSet = ("line 1" :: "line 2" :: "line 3":: "line 3" :: Nil).toSet
    demoCollectionSet.foreach(x=>println(x))

    case class testcaseclass(x: String, y: Int)
    val test1 = new testcaseclass("1", 1)
    val test2 = new testcaseclass("2", 2)
    val test3 = new testcaseclass("3", 2)
    val test4 = new testcaseclass("4", 4)
    val uniqRes = (test1::test2::test3::test4::Nil).groupBy(testclass=>testclass.y).map(testclass => testclass._2.head)

    val iter = demoCollectionList1.iterator
    while(iter.hasNext)
      println(iter.next)

    println("demo colelction folds")
    val demoCollection = 1 :: 2 :: 3 :: 4 :: Nil
    println(s"fold result: ${demoCollection.fold(0)((z, i) => z+i)}")
    println(s"fold left result: ${demoCollection.foldLeft(0)((z, i) => z+i)}")
    println(s"reduce result: ${demoCollection.reduce((z, i) => z+i)}")

    val test = List(1,2,3,4,5) :: List(1,50,3) :: List(1,2) :: Nil
    test.filter(x => x.sum > 10).foreach(x=>println(x.mkString(",")))






  }

}