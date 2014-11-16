package com.todesking

package object scalapp {
  implicit class any2pp[A](self: A) {
    def pp(log: String => Unit = println(_)): A = {
      log(ScalaPP.format(self))
      self
    }
  }
}

