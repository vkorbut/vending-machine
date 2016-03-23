package com.github.vkorbut.fctryout.hardware

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */
trait MachineHardwareInterface {
  val buttonNames: Seq[String]

  def logError(message: String)

  def returnChange(amount: BigDecimal): Unit

  def cashAcceptorMode(active: Boolean)

  def buttonHighlightMode(buttonName: String, highlighted: Boolean)

  def buttonEnableMode(buttonName: String, enabled: Boolean)

  def prepareDrink(product: String)

  val hwEvent: Event => Unit
}
