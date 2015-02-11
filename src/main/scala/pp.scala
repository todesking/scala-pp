package com.todesking.scalapp

object ScalaPP {
  def format(value: Any)(implicit format: Format = defaultFormat): String = {
    format(value)
  }

  def pp(value: Any)(implicit format: Format = defaultFormat, out: Out = defaultOut): Unit = {
    out(format(value))
  }

  val defaultFormat = new DefaultFormat(80, false)
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
      case m: Map[_, _] =>
        // TODO
        Text(m.map{ case(k, v) => apply(k) -> apply(v) }.toString)
      case s: Seq[_] =>
        // TODO
        Text(s.map(apply(_)).toString)
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

  def buildDocFromNamedProperties(name: String, properties: Seq[(String, Doc)], op: String): Doc = {
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
              Group(Text(propName) ^^ Text(" ") ^^ Text(op)) ^| Group(value) ^^ suffix
            })
          } else {
            Group(value) ^^ suffix
          }
        }.foldLeft[Doc](Nil){(a, x) => a ^| x}
      }) ^^ Break("") ^^ Text(")")
    }
  }

  def buildDocFromCaseClass(cc: CaseClass): Doc = {
    buildDocFromNamedProperties(
      cc.name,
      cc.members.map{case (name, value) => name -> buildDoc(value)},
      "="
    )
  }

  def needMultiLineFormat(cc: CaseClass): Boolean =
    cc.members.size > 1 && cc.members.exists {case (name, value) => asCaseClass(value).nonEmpty}

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
    def pp(implicit format: Format = ScalaPP.defaultFormat, out: Out = ScalaPP.defaultOut): A = {
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

