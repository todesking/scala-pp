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
      it("should format literals") {
        ScalaPP.format(1) shouldEqual "1"
        ScalaPP.format("foo") shouldEqual "\"foo\""
        ScalaPP.format("\"") shouldEqual "\"\\\"\""
      }

      it("should format other objects with toString()") {
        val obj = new AnyRef()
        ScalaPP.format(obj) shouldEqual obj.toString
      }
      describe("with case classes") {
        import CaseClassesForTest._
        implicit val format = new DefaultFormat(showMemberName = true)

        it("should format the same as default toString() if the object is simple enough") {
          ScalaPP.format(SNil) shouldEqual "SNil"
          ScalaPP.format(Cons(Atom(1), Atom(2)))
        }

        it("should format with indent if the object has > 1 fields and any field's value is nested") {
          val complex = Cons(Atom(1), Cons(Atom(2), SNil))
          ScalaPP.format(complex) shouldEqual """
            |Cons(
            |  car = Atom(value = 1),
            |  cdr = Cons(
            |    car = Atom(value = 2),
            |    cdr = SNil
            |  )
            |)""".stripMargin.trim
        }

        it("should format to pretty style") {
          ScalaPP.format(Cons(Cons(Atom(1), Atom(2)), Atom(3))) shouldEqual """
          |Cons(
          |  car = Cons(
          |    car = Atom(value = 1),
          |    cdr = Atom(value = 2)
          |  ),
          |  cdr = Atom(value = 3)
          |)
          """.stripMargin.trim

          ScalaPP.format(
            Cons(Cons(Atom(1), Atom(2)), Cons(Atom(2), Cons(Atom(3), Cons(Atom(4), SNil))))
          ) shouldEqual """
            |Cons(
            |  car = Cons(
            |    car = Atom(value = 1),
            |    cdr = Atom(value = 2)
            |  ),
            |  cdr = Cons(
            |    car = Atom(value = 2),
            |    cdr = Cons(
            |      car = Atom(value = 3),
            |      cdr = Cons(
            |        car = Atom(value = 4),
            |        cdr = SNil
            |      )
            |    )
            |  )
            |)""".stripMargin.trim
        }

        it("should format without member name if option is set") {
          implicit val format = new DefaultFormat(showMemberName = false)

          ScalaPP.format(Cons(Cons(Atom(1), Atom(2)), SNil)) shouldEqual """
          |Cons(
          |  Cons(
          |    Atom(1),
          |    Atom(2)
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


