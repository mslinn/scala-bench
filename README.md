# scala-bench

This is the test program that was described in Li Haoyi's article entitled 
[Benchmarking Scala Collections](http://www.lihaoyi.com/post/BenchmarkingScalaCollections.html).

Mike Slinn updated the code so it would run on Scala 2.13.0.

**This code should be carefully reviewed before its results are taken seriously.**
Comments are welcome on the [issues page](https://github.com/mslinn/scala-bench/issues).

## New Benchmarks
New benchmarks for the following collection types were added.

### Mutable
`AnyRefMap`, `ArrayBuffer`, `ArrayDeque`, `ArraySeq`, `CollisionProofHashMap`, `HashMap`, `HashSet`, `LinkedHashMap`, `LinkedHashSet`, and `ListBuffer`.

### Immutable
`HashMap`, `ListMap`, `ListSet`, `SeqMap`, `TreeSeqMap`, TreeMap`, `TreeSet`, and `VectorMap`.

## TODO

Other types that are not currently benchmarked include:

### Mutable
`BitSet`, `IntMap`, and `LongMap`.

### Immutable
`BitSet`.
