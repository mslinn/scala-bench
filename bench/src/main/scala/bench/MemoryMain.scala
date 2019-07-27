package bench

import java.text.NumberFormat
import java.util.Locale
import scala.collection.{immutable, mutable}
import scala.collection.parallel._
import scala.jdk.CollectionConverters._

object MemoryMain {
  implicit val objectOrdering: Ordering[Object] = Ordering.by { _.toString }

  def main(args: Array[String]): Unit = {
    def obj = new Object()

    def nums[T](n: Int, f: Int => T): Iterator[T] = (0 until n).iterator.map(f)
    def parNums[T](n: Int, f: Int => T): ParIterable[T] = (0 until n).to(ParIterable).map(f)

    val collections = Seq[(String, Int => AnyRef)](
      ("List", nums(_, _ => obj).toList),
      ("Vector", nums(_, _ => obj).toVector),
      ("UnforcedStream", n => Stream(nums(n, _ => obj))), // Is there any performance difference between Stream and LazyList?
      ("ForcedStream", { n =>
        val x = Stream(nums(n, _ => obj))
        x.foreach(_ => ())
        x
      }),
      ("UnforcedLazyList", n => LazyList(nums(n, _ => obj))),
      ("ForcedLazyList", { n =>
        val x = LazyList(nums(n, _ => obj))
        x.foreach(_ => ())
        x
      }),
//      ("BitSet", n => immutable.BitSet.fromSpecific(nums(n, _ => obj).asInstanceOf[Iterator[Int]])), // meaningless garbage
      ("ListSet", n => immutable.ListSet(nums(n, _ => obj))),
      ("Set", nums(_, _ => obj).toSet),
      ("TreeSet", n => immutable.TreeSet.from(nums(n, _ => obj))),
      ("SortedSet", n => immutable.SortedSet(nums(n: Int, identity).toSeq: _*)), // should be identical to TreeSet

      ("HashMap", n => immutable.HashMap.from(nums(n, _ => (obj, obj)))),
      ("ListMap", n => immutable.ListMap.from(nums(n, _ => (obj, obj)))),
      ("Map", nums(_, _ => (obj, obj)).toMap),
      ("TreeMap", n => immutable.TreeMap.from(nums(n, _ => (obj, obj)))),
      ("TreeSeqMap", n => immutable.TreeSeqMap.from(nums(n, _ => (obj, obj)))),
      ("SeqMap", n => immutable.SeqMap.from(nums(n, _ => (obj, obj)))),
      ("VectorMap", n => immutable.VectorMap.from(nums(n, _ => (obj, obj)))),
      ("Queue", immutable.Queue[AnyRef](nums(_: Int, _ => obj))),

      ("m.AnyRefMap", n => mutable.AnyRefMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.ArrayBuffer", n => mutable.ArrayBuffer(nums(n, _ => obj))),
      ("m.ArrayDeque", n => mutable.ArrayDeque(nums(n, _ => obj))),
      ("m.ArraySeq", n => mutable.ArraySeq(nums(n, _ => obj))),

      ("m.Set", n => mutable.Set(nums(n, _ => obj))),
      ("m.HashSet", n => mutable.HashSet(nums(n, _ => obj))),
      ("m.LinkedHashSet", n => mutable.LinkedHashSet(nums(n, _ => obj))),

      ("m.Buffer", n => mutable.Buffer(nums(n, _ => obj))),
      ("m.ListBuffer", n => mutable.ListBuffer(nums(n, _ => obj))),

      ("m.Map", n => mutable.Map(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.CollisionProofHashMap", n => mutable.CollisionProofHashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.HashMap", n => mutable.HashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.LinkedHashMap", n => mutable.LinkedHashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.PriQueue", n => mutable.PriorityQueue(nums(n, identity).toSeq: _*)),
      ("m.Queue", n => mutable.Queue(nums(n, _ => obj))),
      ("m.SortedSet", n => mutable.SortedSet(nums(n, identity).toSeq: _*)),
      ("m.Stack", n => mutable.Stack(nums(n, _ => obj))),

      ("String", "1" * _),

      ("Array", nums(_, _ => obj).toArray),
      ("ArraySeq", n => immutable.ArraySeq(nums(n, _ => obj))),

      ("ArrayBoolean", nums(_, _ % 2 == 0).toArray),
      ("ArrayByte", nums(_, _.toByte).toArray),
      ("ArrayShort", nums(_, _.toShort).toArray),
      ("ArrayInt", nums(_, _.asInstanceOf[Int]).toArray),
      ("ArrayLong", nums(_, _.toLong).toArray),

      ("BoxArrayBoolean", nums(_, x => (x % 2 == 0).asInstanceOf[AnyRef]).toArray),
      ("BoxArrayByte", nums(_, _.toByte.asInstanceOf[AnyRef]).toArray),
      ("BoxArrayShort", nums(_, _.toShort.asInstanceOf[AnyRef]).toArray),
      ("BoxArrayInt", nums(_, _.toInt.asInstanceOf[AnyRef]).toArray),
      ("BoxArrayLong", nums(_, _.toLong.asInstanceOf[AnyRef]).toArray),

      ("j.List", nums(_, _.toLong.asInstanceOf[AnyRef]).toBuffer.asJava: java.util.List[AnyRef]),
      ("j.Map", n => mutable.Map(nums(n, _ => (obj, obj)).toSeq: _*).asJava: java.util.Map[AnyRef, AnyRef]),
      ("j.Set", nums(_, _ => obj).to(mutable.Set).asJava: java.util.Set[AnyRef]),

      ("p.Set", parNums(_, _ => obj).to(immutable.Set).asJava: java.util.Set[AnyRef]),
      ("p.Map", n => ParMap(parNums(n, _ => (obj, obj)).to(ParMap).seq.toSeq: _*))
    )
    val sizes = Seq(0, 1, 4, 16, 64, 256, 1024, 4069, 16192, 65536, 262144, 1048576)
    val results = for ((name, factory) <- collections) yield {
      val numbers = for (n <- sizes) yield DeepSize(factory(n))
      (name, numbers)
    }

    def printRow[I: Integral](name: String, items: Seq[I]): Unit = {
      val width = 15
      println(
        name.padTo(width, ' ') +
          items.map(NumberFormat.getNumberInstance(Locale.US).format)
            .map(_.reverse.padTo(width, ' ').reverse).mkString
      )
    }

    printRow("Size", sizes)
    println()
    for ((name, numbers) <- results) {
      printRow(name, numbers)
    }
  }
}
