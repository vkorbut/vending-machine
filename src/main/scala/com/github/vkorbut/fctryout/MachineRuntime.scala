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

class MachineRuntime(hardware: MachineHardwareInterface, val productPrices: Map[String, BigDecimal]) {
  hardware.hwEventHandler = Option(onEvent _)

  var currentState: State = ProductSelection(0, None)

  def onEvent(event: Event): Unit = {
    currentState = actions
      .find(_.isDefinedAt((currentState, event)))
      .map(_.apply(currentState, event))
      .getOrElse(currentState)
  }

  def productPrice = productPrices.get _

  type Action = PartialFunction[(State, Event), State]

  val cancelAction: Action = {
    case (ProductSelection(deposit, product), CancelPressed()) =>
      product.foreach(hardware.setButtonHighlighted(_, highlighted = false))
      hardware.setCashAcceptorEnabled(false)
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
    case (ProductSelection(deposit, None), CashCommitted(amount)) =>
      val newDeposit = deposit + amount
      hardware.returnChange(newDeposit)
      ReturningChange(newDeposit, newDeposit)

    case (currentState@ProductSelection(deposit, Some(product)), CashCommitted(amount)) =>
      val newDeposit = deposit + amount
      if (productPrice(product).filter(_ <= newDeposit).isDefined) {
        hardware.prepareAndSellProduct(product)
        Selling(newDeposit, product)
      } else {
        currentState.copy(deposit = newDeposit)
      }

    case (s: Selling, CashCommitted(amount)) => s.copy(deposit = s.deposit + amount)
    case (s: ReturningChange, CashCommitted(amount)) => s.copy(change = s.change + amount)
  }

  val sellingCompleteAction: Action = {
    case (Selling(deposit, product), SellingComplete()) =>
      val change = productPrice(product) match {
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
    hardware.buttonNames.foreach(hardware.setButtonHighlighted(_, highlighted = false))
  }

  val actions = Seq(
    selectProductAction,
    cancelAction,
    commitCashAction,
    sellingCompleteAction,
    changeReturnedAction)
}