package com.todesking.scalapp

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.Assertions._

class PPSpec extends FunSpec with Matchers {
  sealed class MyList
  case class MyNode(v: Any, next: MyList) extends MyList
  case object MyNil extends MyList

  describe("PP") {
    describe("#format()") {
      it("should format literals") {
        PP.format(1) shouldEqual "1"
        PP.format("foo") shouldEqual "\"foo\""
        PP.format("\"") shouldEqual "\"\\\"\""
      }
      it("should format other objects with toString()") {
        val obj = new AnyRef()
        PP.format(obj) shouldEqual obj.toString
      }
    }
  }
}


