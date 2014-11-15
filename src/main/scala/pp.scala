package com.todesking.scalapp

object PP {
  def format(value: Any): String = {
    value match {
      case str:String => s""""${str.replaceAll("\"", "\\\\\"")}""""
      case x => x.toString
    }
  }
}
