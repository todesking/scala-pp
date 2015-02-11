# scala-pp: Pretty printing Scala objects

It's like the Ruby's `pp` and `tapp`.

Also provide `tap` method.



## Requirements

Scala 2.11.x (Unfortunately, 2.10.x is not supported yet)

## Status

Under development, API will change.

* TODO: Support collections
* TODO: Respect user-defined toString() in case classes
* TODO: Customizable formatter

## Install

```scala
resolvers += "com.todesking" at "http://todesking.github.io/mvn/"

addSbtPlugin("com.todesking" %% "scala-pp" % "0.0.3")
```

## Usage

### Basic

```scala
import com.todesking.scalapp.ScalaPP

ScalaPP.pp(1)

val pretty: String = ScalaPP.format(1)
```

```scala
import com.todesking.scalapp.syntax._ // Enable any.pp, any.tap

scala> 1.pp
1
res0: Int = 1


scala> "foo".pp
"foo"
res1: String = foo

scala> "foo".tap(_.size.pp)
3
res2: String = foo
```

```scala
scala> import com.todesking.scalapp.syntax._

scala> trait Tree
scala> case class Node(l: Tree, r: Tree) extends Tree
scala> case class Leaf(value: Any) extends Tree

scala> Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9))).pp()
Node(
  Leaf(1),
  Node(Node(Leaf(1), Node(Leaf(1), Leaf(2))), Leaf(9))
)
res4: Node = Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9)))

scala> val r = new scala.util.Random
scala> (1 to 20).map { _ => r.nextInt }.pp
Vector(
  -154732736,
  -1175868887,
  -1154500417,
  -224679654,
  -1673519284,
  -45175998,
  -1622246443,
  -377114259,
  -594461594,
  -1236591375,
  -367565645,
  1874916582,
  -784108643,
  -676509857,
  -580471591,
  -295290196,
  2101491230,
  1757271540,
  -2072303228,
  -606515791
)
```

### Format option

```scala
scala> implicit val format = new com.todesking.scalapp.DefaultFormat(showMemberName = true)
scala> Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9))).pp()
Node(
  l = Leaf(value = 1),
  r =
    Node(
      l =
        Node(
          l = Leaf(value = 1),
          r =
            Node(
              l = Leaf(value = 1),
              r = Leaf(value = 2)
            )
        ),
      r = Leaf(value = 9)
    )
)
res1: Node = Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9)))
```

### Change output destination

```scala
// pp via stderr(default is stdout)
implicit val out = com.todesking.scalapp.Out.stderr
```
