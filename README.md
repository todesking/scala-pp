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
trait Tree
case class Node(l: Tree, r: Tree) extends Tree
case class Leaf(value: Any) extends Tree

scala> Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9))).pp()
Node(
  Leaf(1),
  Node(
    Node(
      Leaf(1),
      Node(
        Leaf(1),
        Leaf(2)
      )
    ),
    Leaf(9)
  )
)
res4: Node = Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9)))
```

### Format option

```scala
scala> implicit val format = new com.todesking.scalapp.DefaultFormat(showMemberName = true)
scala> Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9))).pp()
Node(
  l = Leaf(value = 1),
  r = Node(
    l = Node(
      l = Leaf(value = 1),
      r = Node(
        l = Leaf(value = 1),
        r = Leaf(value = 2)
      )
    ),
    r = Leaf(value = 9)
  )
)
```

### Change output destination

```scala
// pp via stderr(default is stdout)
implicit val out = com.todesking.scalapp.Out.stderr
```
