package com.github.vkorbut.fctryout.hardware

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */
trait Event
case class CancelPressed() extends Event
case class ProductSelected(productName:String) extends Event
case class CashCommitted(amount:BigDecimal) extends Event
case class BrewingComplete() extends Event
case class ChangeReturned(amount:BigDecimal) extends Event