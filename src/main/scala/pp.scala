package com.todesking.scalapp

object ScalaPP {
  def format(value: Any): String = {
    val formatter = new Layout(80, 0)
    format(value, formatter)
    formatter.toString.trim
  }

  def format(value: Any, formatter: Layout): Unit = {
    value match {
      case str:String =>
        formatter.appendUnbreakable(s""""${str.replaceAll("\"", "\\\\\"")}"""")
      case x =>
        asCaseClass(x).map(formatCaseClass(_, formatter)) getOrElse formatter.appendUnbreakable(x.toString)
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

  def formatCaseClass(cc: CaseClass, formatter: Layout): Unit = {
    if(needMultiLineFormat(cc)) {
      formatter.appendRaw(cc.name)
      formatter.appendRaw("(")
      formatter.terminateLine()
      formatter.withIndent(2) {
        cc.members.zipWithIndex.foreach { case ((name, value), i) =>
          formatter.appendRaw(name)
          formatter.appendRaw(" = ")
          format(value, formatter)
          if(i < cc.members.size - 1)
            formatter.appendRaw(", ")
          formatter.terminateLine()
        }
      }
      formatter.terminateLine()
      formatter.appendRaw(")")
      formatter.terminateLine()
    } else {
      formatter.appendRaw(cc.name)
      formatter.appendRaw("(")
      cc.members.zipWithIndex.foreach { case ((name, value), i) =>
        formatter.appendRaw(name)
        formatter.appendRaw(" = ")
        format(value, formatter)
        if(i < cc.members.size - 1)
          formatter.appendRaw(", ")
      }
      formatter.appendRaw(")")
    }
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

  // TODO: This Layout class is from com.todesking % dox. That should be independent library.
  class Layout(optimalWidth:Int, private var indentLevel:Int) {
    import scala.collection.mutable
    private var lines = mutable.ArrayBuffer.empty[String]
    private var currentLine:String = ""
    private var cancelNextSpacing = false

    def cancelSpacing():Unit = {
      cancelNextSpacing = true
      currentLine = currentLine.replaceAll("""\s+\z""", "")
    }

    def requireEmptyLines(n:Int):Unit = {
      terminateLine()
      val emptyLines = lines.reverse.takeWhile(_.isEmpty).size
      0 until ((n - emptyLines) max 0) foreach { _=> newLine() }
    }

    def restWidth:Int = optimalWidth - indentLevel

    override def toString() =
      lines.mkString("\n") + currentLine + "\n"

    def appendRaw(str: String): Unit =
      currentLine += str

    def appendText(str:String):Unit =
      doMultiLine(str)(appendBreakable0)

    def appendUnbreakable(str:String):Unit =
      doMultiLine(str)(appendUnbreakable0(_, needSpacing = true))

    def appendEqualSpaced(parts:String*):Unit = {
      parts.size match {
        case 0 => newLine()
        case 1 =>
          val pad = " " * ((restWidth - width(parts(0))) / 2).max(0)
          appendUnbreakable(pad + parts(0))
        case _ =>
          val pad = " " * ((restWidth - parts.map(width(_)).sum) / (parts.size - 1)).max(1)
          appendUnbreakable(parts.mkString(pad))
      }
    }

    private[this] def doMultiLine(str:String)(f:String => Unit):Unit = {
      val lines = str.split("\n")
      assert(lines.nonEmpty)
      lines.dropRight(1).foreach {line =>
        f(line)
        newLine()
      }
      f(lines.last)
    }

    private[this] def hasCurrentLineContent():Boolean =
      currentLine.nonEmpty && currentLine != " " * indentLevel

    def indent(n:Int):Unit = {
      require(indentLevel + n >= 0)
      if(!hasCurrentLineContent) {
        indentLevel += n
        currentLine = " " * indentLevel
      } else {
        indentLevel += n
      }
    }

    def withIndent(n:Int)(f: =>Unit):Unit = {
      indent(n)
      f
      indent(-n)
    }

    def terminateLine():Unit = {
      if(hasCurrentLineContent) {
        newLine()
      }
    }

    def newLine():Unit = {
      lines += currentLine.replaceAll("""\s+$""", "")
      currentLine = " " * indentLevel
    }

    private[this] def appendBreakable0(str:String):Unit = {
      val words = str.split("""\s+""").filter(_.nonEmpty)
      words.foreach { word =>
        appendUnbreakable0(word, needSpacing = true)
      }
    }

    private[this] def appendUnbreakable0(str:String, needSpacing:Boolean):Unit = {
      if(!hasCurrentLineContent) {
        currentLine += str
      } else if(needSpacing && !cancelNextSpacing && needSpacingBeyond(currentLine.last, str)) {
        val spaced = " " + str
        if(needNewLine(width(spaced))) {
          newLine()
          currentLine += str
        } else {
          currentLine += spaced
        }
      } else if(needNewLine(width(str)) && canNextLine(str)) {
        newLine()
        currentLine += str
      } else {
        currentLine += str
      }
      cancelNextSpacing = false
    }

    private[this] def needSpacingBeyond(c:Char, s:String):Boolean = {
      s.nonEmpty && (s(0) match {
        case ' ' | ')' | ']' | ';' | '.' | ',' => false
        case _ => true
      }) && (c match {
        case ' ' | '[' | '(' => false
        case _ => true
      })
    }

    private[this] def needNewLine(w:Int):Boolean =
      indentLevel < width(currentLine) && w + width(currentLine) > optimalWidth

    private[this] def canNextLine(s:String):Boolean =
      "!.,:;)]}>".contains(s(0)).unary_!

    def width(line:String):Int = {
      line.length
    }
  }
}
