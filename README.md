# scala-pp: Pretty printing Scala objects

It's like the Ruby's `pp`

## Install

```scala
resolvers += "com.todesking" at "http://todesking.github.io/mvn/"

addSbtPlugin("com.todesking" %% "scala-pp" % "0.0.1")
```

## Usage

```scala
scala> import com.todesking.scalapp._

scala> 1.pp()
1
res0: Int = 1


scala> "foo".pp()
"foo"
res1: String = foo


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

// Format style could change via implicit variable
// NOTE: Make sure implicit val name to `defaultscalapp` to avoid conflict
scala> implicit val defaultScalaPP = new ScalaPP(showMemberName = true)
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
