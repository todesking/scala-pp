package com.todesking

package object scalapp {
  implicit val defaultScalaPP = new ScalaPP(showMemberName = false)
  implicit class any2pp[A](self: A) {
    def pp(log: String => Unit = println(_))(implicit format: ScalaPP): A = {
      log(ScalaPP.format(self)(format))
      self
    }
  }
}

