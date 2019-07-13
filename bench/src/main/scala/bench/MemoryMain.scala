package bench

import java.text.NumberFormat
import java.util.Locale
import scala.jdk.CollectionConverters._
import scala.collection.immutable.Queue
import scala.collection.{SortedSet, immutable, mutable}

object MemoryMain {
  implicit val objectOrdering: Ordering[Object] = Ordering.by { _.toString }

  def main(args: Array[String]): Unit = {
    def obj = new Object()

    def nums[T](n: Int, f: Int => T): Iterator[T] = (0 until n).iterator.map(f)

    val collections = Seq[(String, Int => AnyRef)](
      ("Vector", nums(_, _ => obj).toVector),
      ("Array", nums(_, _ => obj).toArray),
      ("ArraySeq", n => immutable.ArraySeq(nums(n, _ => obj))),
      ("List", nums(_, _ => obj).toList),
      ("UnforcedStream", n => Stream(nums(n, _ => obj))), // curious to know if there is any performance difference between Stream and LazyList
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
      ("Set", nums(_, _ => obj).toSet),
      ("ListSet", n => immutable.ListSet(nums(n, _ => obj))),
      ("TreeSet", n => immutable.TreeSet.from(nums(n, _ => obj))),
      ("Map", nums(_, _ => (obj, obj)).toMap),
      ("HashMap", n => immutable.HashMap.from(nums(n, _ => (obj, obj)))),
      ("TreeMap", n => immutable.TreeMap.from(nums(n, _ => (obj, obj)))),
      ("TreeSeqMap", n => immutable.TreeSeqMap.from(nums(n, _ => (obj, obj)))),
      ("ListMap", n => immutable.ListMap.from(nums(n, _ => (obj, obj)))),
      ("SeqMap", n => immutable.SeqMap.from(nums(n, _ => (obj, obj)))),
      ("VectorMap", n => immutable.VectorMap.from(nums(n, _ => (obj, obj)))),

      ("SortedSet", n => immutable.SortedSet(nums(n: Int, identity).toSeq: _*)),
      ("Queue", Queue[AnyRef](nums(_: Int, _ => obj))),

      ("m.ArraySeq", n => mutable.ArraySeq(nums(n, _ => obj))),
      ("m.ArrayBuffer", n => mutable.ArrayBuffer(nums(n, _ => obj))),
      ("m.ArrayDeque", n => mutable.ArrayDeque(nums(n, _ => obj))),
      ("m.Buffer", n => mutable.Buffer(nums(n, _ => obj))),
      ("m.ListBuffer", n => mutable.ListBuffer(nums(n, _ => obj))),
      ("m.AnyRefMap", n => mutable.AnyRefMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.Map", n => mutable.Map(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.CollisionProofHashMap", n => mutable.CollisionProofHashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.HashMap", n => mutable.HashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.LinkedHashMap", n => mutable.LinkedHashMap(nums(n, _ => (obj, obj)).toSeq: _*)),
      ("m.Set", n => mutable.Set(nums(n, _ => obj))),
      ("m.HashSet", n => mutable.HashSet(nums(n, _ => obj))),
      ("m.LinkedHashSet", n => mutable.LinkedHashSet(nums(n, _ => obj))),
      ("m.Queue", n => mutable.Queue(nums(n, _ => obj))),
      ("m.PriQueue", n => mutable.PriorityQueue(nums(n, identity).toSeq: _*)),
      ("m.Stack", n => mutable.Stack(nums(n, _ => obj))),
      ("m.SortedSet", n => mutable.SortedSet(nums(n, identity).toSeq: _*)),

      ("String", "1" * _),

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
      ("j.Set", nums(_, _ => obj).to(mutable.Set).asJava: java.util.Set[AnyRef])
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
