package com.todesking.scalapp

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

object CaseClassesForTest {
  sealed class S
  case class Atom(value: Any) extends S
  case class Cons(car: S, cdr: S) extends S
  case object SNil extends S
}

class PPSpec extends FunSpec with Matchers {
  describe("ScalaPP") {
    describe("#format()") {
      implicit val format = new DefaultFormat(width = 20, showMemberName = true)
      it("should format literals") {
        ScalaPP.format(1) shouldEqual "1"
        ScalaPP.format("foo") shouldEqual "\"foo\""
        ScalaPP.format("\"") shouldEqual "\"\\\"\""
      }

      it("should format other objects with toString()") {
        val obj = new AnyRef()
        ScalaPP.format(obj) shouldEqual obj.toString
      }
      it("should format Array") {
        ScalaPP.format(Array(1, 2, 3)) shouldEqual "Array(1, 2, 3)"
      }
      it("should format empty List") {
        ScalaPP.format(List.empty) shouldEqual "List()"
      }
      it("should format List") {
        ScalaPP.format(List(1)) shouldEqual "List(1)"

        // 1    6    11   16   21
        ScalaPP.format(List(1, 2, 3, 4, 5, 6)) shouldEqual """
          |List(
          |  1, 2, 3, 4, 5, 6
          |)
        """.stripMargin.trim
      }
      it("should format Stream") {
        ScalaPP.format(Stream(1, 2, 3)) shouldEqual """
          |Stream(1, ?)
        """.stripMargin.trim
      }
      it("should format Map") {
        // 1    6    11   16   21
        ScalaPP.format(Map(1 -> "foo", 2 -> "bar")) shouldEqual """
          |Map(
          |  1 -> "foo",
          |  2 -> "bar"
          |)
        """.stripMargin.trim
      }
      describe("with case classes") {
        import CaseClassesForTest._
        implicit val format = new DefaultFormat(width = 20, showMemberName = true)

        it("should format the same as default toString() if the object is simple enough") {
          ScalaPP.format(SNil) shouldEqual "SNil"
        }

        it("should format with indent") {
          val complex = Cons(Atom(1), Cons(Atom(2), SNil))
          // 1    6    11   16   21
          ScalaPP.format(complex) shouldEqual """
            |Cons(
            |  car =
            |    Atom(value = 1),
            |  cdr =
            |    Cons(
            |      car =
            |        Atom(
            |          value = 2
            |        ),
            |      cdr = SNil
            |    )
            |)""".stripMargin.trim
        }

        it("should format without member name if option is set") {
          implicit val format = new DefaultFormat(width = 20, showMemberName = false)
          println(ScalaPP.format(Cons(Cons(Atom(1), Atom(2)), SNil)))

          // 1    6    11   16   21
          ScalaPP.format(Cons(Cons(Atom(1), Atom(2)), SNil)) shouldEqual """
            |Cons(
            |  Cons(
            |    Atom(1), Atom(2)
            |  ),
            |  SNil
            |)
          """.stripMargin.trim
        }
      }
    }
  }
  describe("Any#pp") {
    import com.todesking.scalapp.syntax.Pp
    implicit val out = com.todesking.scalapp.Out.nullOut

    it("should return this") {
      1.pp shouldEqual 1
    }
    it("should print formatted representation of this") {
      implicit val out = Out.capture()
      "123".pp
      out.content shouldEqual "\"123\""
    }
  }
  describe("Any#tap") {
    it("should execute block and return this") {
      import com.todesking.scalapp.syntax.Tap
      var value: Any = null

      1.tap{ n => value = n } shouldEqual 1

      value shouldEqual 1
    }
  }
}


