package bench

import java.text.NumberFormat
import java.util.Locale
import ammonite.ops._
import scala.collection.mutable

object PerfMain extends Excel {
  def main(args: Array[String]): Unit = {
    val testMode = args.contains("-t") // -t command line switch enables test mode
    val width = 15

    def formatItems[I: Integral](name: String, items: Seq[I]): String =
      name.padTo(width, ' ') +
        items.map(NumberFormat.getNumberInstance(Locale.US).format)
          .map(_.reverse.padTo(width, ' ').reverse).mkString

    def printRow[I: Integral](name: String, items: Seq[I]): Unit = println(formatItems(name, items))

    // How large the collections will be in each benchmark
    val sizes = if (testMode) Seq(4) else Seq(0, 1, 4, 16, 64, 256, 1024, 4096, 16192, 65536, 262144, 1048576)

    // How many times to repeat each benchmark
    val repeats = if (testMode) 1 else 7

    // How long each benchmark runs, in millis
    val duration = if (testMode) 50 else 2000

    // How long a benchmark can run before we stop incrementing it
    val cutoff = if (testMode) 400 else 400 * 1000 * 1000

    cellManager.nextRow()
    cellManager.newHeaderCell("Size " + sizes.mkString(", "))

    printRow("Size", sizes)
    val output = mutable.Map.empty[(String, String, Long), mutable.Buffer[Long]]
    val cutoffSizes = mutable.Map.empty[(String, String), Int]
    for (i <- 1 to repeats) {
      cellManager.nextRow()
      cellManager.newDataCell(s"Run $i")
      println("Run " + i)
      for (benchmark <- Benchmark.benchmarks) {
        cellManager.nextRow()
        cellManager.newDataCell(benchmark.name)
        println()
        println(benchmark.name)
        println()
        for (bench <- benchmark.cases) {
          val key = benchmark.name -> bench.name
          val times =
            for (size <- sizes if !(cutoffSizes.getOrElse(key, Int.MaxValue) < size)) yield {
              val buf = output.getOrElseUpdate((benchmark.name, bench.name, size.toLong), mutable.Buffer())

              def handle(run: Boolean): (Int, Long) = {
                System.gc()

                val start = System.currentTimeMillis()
                var count = 0
                while (System.currentTimeMillis() - start < duration) {
                  if (run) bench.run(size)
                  else bench.initializer(size)
                  count += 1
                }
                val end = System.currentTimeMillis()
                (count, end - start)
              }

              val (initCounts, initTime) = handle(run = false)
              val (runCounts, runTime) = handle(run = true)
              val res = ((runTime.toDouble / runCounts - initTime.toDouble / initCounts) * 1000000).toLong
              buf.append(res)
              if (res > cutoff) {
                cutoffSizes(key) = math.min(
                  cutoffSizes.getOrElse(key, Int.MaxValue),
                  size
                )
              }
              res
            }
          printRow(bench.name, times)
          cellManager.nextRow()
          cellManager.newDataCell(bench.name)
          times.foreach { time => cellManager.newDataCell(time.toLong) }
        }
      }
    }
    rm(pwd / Symbol("target") / "results.json")
    write(
      pwd / Symbol("target") / "results.json",
      upickle.default.write(output.view.mapValues(_.toList).toMap)
    )
    output.view.mapValues(_.toList).toMap.foreach { case (k, v) =>
      cellManager.nextRow()
      cellManager.newDataCell(k._1)
      cellManager.newDataCell(k._2)
      cellManager.newDataCell(k._3.toLong)
      v.foreach { x => cellManager.newDataCell(x.toDouble) }
    }
    writeFile(".")
  }
}
