package com.github.vkorbut.fctryout.hardware.simulation

import com.github.vkorbut.fctryout.hardware._

import scala.collection.immutable.Queue

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */

trait ConsoleAppHardwareSimulation extends MachineHardwareInterface {
  val buttonNames = 1 to 10 map (_.toString)
  var buttons: Map[String, (Boolean, Boolean)] = buttonNames.map(b => (b, (true, false))).toMap
  var cashAcceptorEnabled = false
  var pendingEvents: Queue[Event] = Queue[Event]()

  override def logError(message: String) = println(s"Error: $message")

  override def returnChange(amount: BigDecimal): Unit = println(s"Returning change: $amount")

  override def setCashAcceptorEnabled(active: Boolean) = {
    cashAcceptorEnabled = active
  }

  def setButtonHighlighted(buttonName: String, value: Boolean) {
    for ((enabled: Boolean, state: Boolean) <- buttons.get(buttonName) if enabled) {
      buttons += (buttonName ->(enabled, value))
    }
  }

  def setButtonEnabled(buttonName: String, value: Boolean) {
    for ((enabled: Boolean, state: Boolean) <- buttons.get(buttonName)) {
      buttons += (buttonName ->(value, state && value))
    }
  }

  def prepareAndSellProduct(product: String) = {
    println(s"Preparing product $product")
    pendingEvents = pendingEvents.enqueue(SellingComplete())
  }

  def cycle(userInput: String) {
    if (!userInput.isEmpty) {
      parseUserInput(userInput).foreach(v => pendingEvents = pendingEvents.enqueue(v))
    }
    while (pendingEvents.nonEmpty) {
      val (event, rest) = pendingEvents.dequeue
      pendingEvents = rest
      hwEvent(event)
      printState()
    }
  }

  def parseUserInput(userInput: String): Option[Event] = {
    userInput.split(' ') match {
      case Array("button", index) if buttonNames.contains(index) => Option(ProductSelected(index))
      case Array("cancel") => Option(CancelPressed())
      case Array("cash", amount) if cashAcceptorEnabled =>
        try {
          val cashAmount: BigDecimal = BigDecimal(amount)
          Option(CashCommitted(cashAmount))
        }
        catch {
          case nfe: NumberFormatException =>
            println("Invalid cash amount " + amount)
            None
        }
      case _ =>
        println(s"Invalid input: $userInput")
        None
    }
  }

  def printState()
}