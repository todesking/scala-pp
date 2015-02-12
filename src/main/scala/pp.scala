package com.todesking.scalapp

object ScalaPP {
  def format(value: Any)(implicit format: Format = defaultFormat): String = {
    format(value)
  }

  def pp(value: Any)(implicit format: Format = defaultFormat, out: Out = defaultOut): Unit = {
    out(format(value))
  }

  val defaultFormat = new DefaultFormat(60, false)
  val defaultOut = Out.stdout
}

trait Out {
  def apply(s: String): Unit
}

class CaptureOut extends Out {
  var content: String = null
  override def apply(s: String) = {
    content = s
  }
}

object Out {
  def apply(f: String => Unit): Out = new Out {
    override def apply(s: String) = f(s)
  }

  val stdout: Out = apply(System.out.println(_))
  val stderr: Out = apply(System.err.println(_))
  val nullOut: Out = apply{ _ => }
  def capture(): CaptureOut = new CaptureOut
}

trait Format {
  def apply(value: Any): String
}

class DefaultFormat(val width: Int = 80, val showMemberName: Boolean = false) extends Format {
  import pretty_printer.{Doc, PrettyPrinter}

  override def apply(value: Any): String = {
    pretty_printer.PrettyPrinter.pretty(width, buildDoc(value))
  }

  def buildDoc(value: Any): Doc = {
    import Doc._
    value match {
      case str:String =>
        Text(s""""${str.replaceAll("\"", "\\\\\"")}"""")
      case c: Char =>
        Text(s"'${c}'")
      case _: Int | Double =>
        Text(value.toString)
      case f: Float =>
        Text(s"${f}f")
      case l: Long =>
        Text(s"${l}L")
      case r: scala.collection.immutable.Range =>
        val op =
          if(r.isInclusive) Text("to")
          else Text("until")
        val by =
          if(r.step != 1) Text("by") ^| buildDoc(r.step)
          else Nil
        buildDocFromRange("Range", buildDoc(r.start), op, buildDoc(r.end), by)
      case r: scala.collection.immutable.NumericRange[_] =>
        val op =
          if(r.isInclusive) Text("to")
          else Text("until")
        val by =
          if(r.step != 1) Text("by") ^| buildDoc(r.step)
          else Nil
        buildDocFromRange("NumericRange", buildDoc(r.start), op, buildDoc(r.end), by)
      case a: Array[_] =>
        buildDocFromValues("Array", a.map(buildDoc(_)))
      case s: Stream[_] =>
        if(s.isEmpty)
          buildDocFromValues(s.stringPrefix, Seq.empty)
        else
          buildDocFromValues(s.stringPrefix, Seq(buildDoc(s.head), Text("?")))
      case m: Map[_, _] =>
        buildDocFromNamedProperties(m.stringPrefix, m.map{ case (k, v) => buildDoc(k) -> buildDoc(v) }, Text("->"))
      case s: Traversable[_] =>
        buildDocFromValues(s.stringPrefix, s.map(buildDoc(_)).toIterable)
      case x =>
        asCaseClass(x).map(buildDocFromCaseClass(_)) getOrElse Text(x.toString)
    }
  }

  def asCaseClass(value: Any): Option[CaseClass] = {
    import scala.reflect.runtime.{universe => ru}

    val universeMirror = ru.runtimeMirror(value.getClass.getClassLoader)
    val instanceMirror = universeMirror.reflect(value)

    if(!isCaseClass(instanceMirror) || isCaseObject(instanceMirror)) {
      None
    } else {
      Some(CaseClass(instanceMirror))
    }
  }

  def isCaseClass(im: scala.reflect.api.Mirrors#InstanceMirror): Boolean =
    im.symbol.isCaseClass

  def isCaseObject(im: scala.reflect.api.Mirrors#InstanceMirror): Boolean =
    // It is very... insufficient. But I dont known what to do right.
    isCaseClass(im) && (im.instance match {
      case prod: Product =>
        prod.productArity == 0 && prod.toString == prod.productPrefix
      case _ =>
        false
    })

  def buildDocFromRange(name: String, start: Doc, op: Doc, end: Doc, by: Doc): Doc = {
    import Doc._
    Text(name) ^^ Text("(") ^^ Nest(2, Break("") ^^ Group(start ^| Group(op ^| end) ^| Group(by))) ^^ Break("") ^^ Text(")")
  }

  def buildDocFromNamedProperties(name: String, properties: Iterable[(Doc, Doc)], arrow: Doc): Doc = {
    import Doc._
    Group {
      Text(name) ^^ Text("(") ^^ Nest(2, Break("") ^^ Group {
        properties.zipWithIndex.map{ case ((propName, value), i) =>
          val suffix =
            if(i < properties.size - 1) {
              Text(",")
            } else {
              Nil
            }
          if(showMemberName) {
            Nest(2, Group {
              Group(propName ^^ Text(" ") ^^ arrow) ^| Group(value) ^^ suffix
            })
          } else {
            Group(value) ^^ suffix
          }
        }.foldLeft[Doc](Nil){(a, x) => a ^| x}
      }) ^^ Break("") ^^ Text(")")
    }
  }

  def buildDocFromValues(name: String, values: Iterable[Doc]): Doc = {
    import Doc._
    Group {
      Text(name) ^^ Text("(") ^^ Nest(2, Break("") ^^ Group {
        values.zipWithIndex.map{ case (value, i) =>
          val suffix =
            if(i < values.size - 1) {
              Text(",")
            } else {
              Nil
            }
          Group(value) ^^ suffix
        }.foldLeft[Doc](Nil){(a, x) => a ^| x}
      }) ^^ Break("") ^^ Text(")")
    }
  }

  def buildDocFromCaseClass(cc: CaseClass): Doc = {
    import Doc._
    buildDocFromNamedProperties(
      cc.name,
      cc.members.map{case (name, value) => Text(name) -> buildDoc(value)},
      Text("=")
    )
  }

  case class CaseClass(mirror: scala.reflect.api.Mirrors#InstanceMirror) {
    def name(): String = mirror.symbol.name.toString
    def members(): Seq[(String, Any)] =
        mirror.symbol.primaryConstructor.asMethod.paramLists(0).map {param =>
          param.name.toString
        }.zip(
          mirror.instance.asInstanceOf[Product].productIterator.toIterable
        ).toSeq
  }
}

object syntax {
  implicit class Pp[A](self: A) {
    def pp()(implicit format: Format = ScalaPP.defaultFormat, out: Out = ScalaPP.defaultOut): A = {
      self.tap(ScalaPP.pp(_))
    }
  }
  implicit class Tap[A](self: A) {
    def tap(f: A => Unit): A = {
      f(self)
      self
    }
  }
}

