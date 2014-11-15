# scala-pp: Pretty printing Scala objects

It's like the Ruby's `pp`

## Install

```scala
resolvers += "com.todesking" at "http://todesking.github.io/mvn/"

addSbtPlugin("com.todesking" %% "scala-pp" % "0.0.1")
```

## Usage

```scala
// Add pp() and tapp() method to any objects
import com.todesking.pp.ForAny._

// All-in-one
import com.todesking.pp._

pp(1)
1

"foo".pp
"foo"

trait Tree
case class Node(l: Tree, r: Tree) extends Node
case class Leaf(value: Any) extends Node

Node(Leaf(1),Node(Node(Leaf(1),Node(Leaf(1),Leaf(2))),Leaf(9))).pp
Node(
  Leaf(1),
  Node(
    Node(
      Leaf(1),
      Node(Leaf(1), Leaf(2))
    ),
    Leaf(9)
  )
)

```
