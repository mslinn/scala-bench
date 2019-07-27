package bench

import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale
import ammonite.ops._
import scala.collection.mutable

/**
  * Created by haoyi on 9/26/16.
  */
object AnalyzeMain {
  def main(args: Array[String]): Unit = {
    val results: Map[(String, String, Long), Vector[Long]] =
      upickle.default.read[Map[(String, String, Long), Vector[Long]]](
        read ! pwd / Symbol("target") / "results.json"
      )

    val grouped: Map[String, Map[String, Map[Long, (Long, String)]]] = {
      results
        .groupBy { case ((bench, _, _), _) => bench }
        .map { case (bench, rest) =>
          bench -> rest.groupBy { case ((_, coll2, _), _) => coll2 }
            .map {
              case (coll: String, rest2: mutable.Map[(String, String, Long), Vector[Long]]) =>
                coll -> rest2.groupBy { case ((_, _, size), _) => size }
                  .view.mapValues { items =>
                    val divisor = (bench, coll) match {
                      case ("lookup", _) => 100
                      case ("foreach", "List-while" | "Array-while" | "m.Buffer") => 100
                      case ("foreach", _) => 10
                      case _ => 1
                    }
                    val sorted: Seq[Long] = items.toVector.flatMap { case ((_, _, _), res) => res }.sorted
                    val middling: Seq[Long] = sorted.drop(1).dropRight(1).map(_ / divisor)
                    val mean: Long = middling.sum / middling.length
                    val stdDev: Long = math.sqrt(middling.map(x => (x - mean) * (x - mean)).sum.toDouble / middling.length.toDouble).toLong
                    val accuracy: Int = math.max(1, math.pow(10, math.log10(stdDev.toDouble)).toInt)

                    val stdDevStr: String = if (stdDev == 0.0) "0%"
                                            else new DecimalFormat("0.0").format(stdDev * 100.0 / math.abs(mean)) + "%"

                    (mean / accuracy * accuracy, stdDevStr)
                  }.toMap

              case (x, y) =>
                sys.error(s"AnalyzeMain.grouped, need to account for ($x, $y)")  // todo write me
            }
        }
    }

    val width = 15
    pprint.pprintln(grouped)
    print("|:" + "-" * width + "-|")
    for (_ <- Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)) {
      print("-" * width + ":|")
    }
    println()
    for ((bench, items) <- grouped) {
      print("| " + " " * width + " |")
      for (_ <- Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)) {
        print(" " * width + " |")
      }
      println()
      print("| " + ("**" + bench + "**").padTo(width, ' ') + " |")
      for (size <- Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)) {
        print(("**" + NumberFormat.getNumberInstance(Locale.US).format(size.toLong) + "**").reverse.padTo(width, ' ').reverse + " |")
      }
      println()
      print("| " + " " * width + " |")
      for (_ <- Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)) {
        print(" " * width + " |")
      }
      println()

      for ((coll, items) <- items) {
        print("| ")
        print(coll.padTo(width, ' '))
        print(" |")
        for (size <- Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)) {
          items.get(size.toLong) match {
            case Some((mean, stdDev)) =>
              //              val ranges = Seq(
              //                1000000000 -> "s",
              //                1000000 -> "ms",
              //                1000 -> "us",
              //                1 -> "ns"
              //              )
              //              val (div, suffix) = ranges.find(_._1 < math.abs(mean)).getOrElse(1 -> "ns")
              val (div, suffix) = (1, "")
              //              val mathContext = new MathContext(2, RoundingMode.DOWN)
              //              val bigDecimal = new java.math.BigDecimal(mean * 1.0 / div, mathContext)

              //              print((bigDecimal.toPlainString() + suffix + " ± " + stdDev).reverse.padTo(width, ' ').reverse + " |")
              print((NumberFormat.getNumberInstance(Locale.US).format(mean) + " ± " + stdDev).reverse.padTo(width, ' ').reverse + " |")
            case None =>
              print(" " * width + " |")
          }

        }
        println()
      }
    }
  }
}
