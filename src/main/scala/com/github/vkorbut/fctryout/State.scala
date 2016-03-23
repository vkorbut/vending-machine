package com.github.vkorbut.fctryout

import com.github.vkorbut.fctryout.hardware._

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */
trait State{
  val deposit:BigDecimal
}

case class ProductSelection(override val deposit:BigDecimal, selected:Option[String]) extends State
case class Selling(override val deposit:BigDecimal, product:String) extends State
case class ReturningChange(override val deposit:BigDecimal, change:BigDecimal) extends State

class Actions(hardware: MachineHardwareInterface) {
  type Action = PartialFunction[(State, Event), State]

  val cancelAction: Action = {
    case (ProductSelection(deposit, product), CancelPressed()) =>
      product.foreach(hardware.setButtonHighlighted(_, highlighted = false))
      hardware.returnChange(deposit)
      ReturningChange(deposit, deposit)
  }

  val selectProductAction: Action = {
    case (ProductSelection(deposit, selected), ProductSelected(productName)) =>
      selected.foreach(hardware.setButtonHighlighted(_, highlighted = false))
      hardware.setButtonHighlighted(productName, highlighted = true)
      hardware.setCashAcceptorEnabled(true)
      ProductSelection(deposit, Option(productName))
  }

  val commitCashAction: Action = {
    case (currentState@ProductSelection(deposit, selected), CashCommitted(amount)) =>
      val newDeposit = deposit + amount
      selected match {
        case None =>
          hardware.returnChange(newDeposit)
          ReturningChange(newDeposit, newDeposit)
        case Some(product) =>
          val price = hardware.priceForProduct(product)
          if (price.isDefined && price.get <= newDeposit) {
            hardware.prepareAndSellProduct(product)
            Selling(newDeposit, product)
          } else {
            currentState
          }
      }
    case (s: Selling, CashCommitted(amount)) => s.copy(deposit = s.deposit + amount)
    case (s: ReturningChange, CashCommitted(amount)) => s.copy(change = s.change + amount)
  }

  val sellingCompleteAction: Action = {
    case (Selling(deposit, product), SellingComplete()) =>
      val change = hardware.priceForProduct(product) match {
        case Some(price) => deposit - price;
        case None =>
          hardware.logError(s"No price for product $product. Assuming 0")
          deposit
      }
      hardware.returnChange(change)
      ReturningChange(deposit, change)
  }

  val changeReturnedAction: Action = {
    case (ReturningChange(_, change), ChangeReturned(amount)) =>
      if (change == amount) {
        resetHardware()
      } else {
        val cannotReturn = change - amount
        hardware.logError(s"Cannot return charge. Required $change but returned $amount, $cannotReturn should be returned")
      }
      ProductSelection(0, None)
  }


  def resetHardware(): Unit = {
    hardware.setCashAcceptorEnabled(false)
    hardware.buttonNames.foreach(hardware.setButtonEnabled(_, enabled = false))
  }

  val actions = Seq(
    selectProductAction,
    cancelAction,
    commitCashAction,
    sellingCompleteAction,
    changeReturnedAction)
}