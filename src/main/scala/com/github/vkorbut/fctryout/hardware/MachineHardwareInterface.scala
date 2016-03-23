package com.github.vkorbut.fctryout.hardware

/**
 * Interface to vending machine's hardware. 
 * 
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */
trait MachineHardwareInterface {
  /**
   * Defines buttons which machine does have. 
   */
  val buttonNames: Seq[String]

  def logError(message: String)

  /**
   * Causes returning money to user.
   * @param amount amount of money to be returned.
   */
  def returnChange(amount: BigDecimal): Unit

  /**
   * Enables/disables cash acceptor making user able (or not able) 
   * to insert money into machine
   * @param active <code>true</code> if cash acceptor should become active, <code> false</code> otherwise. 
   */
  def setCashAcceptorEnabled(active: Boolean)

  def setButtonHighlighted(buttonName: String, highlighted: Boolean)

  /**
   * Used for enabling/disabling buttons due to invalid conditions for products.
   * @param buttonName button to be disabled
   * @param enabled state of the button
   */
  def setButtonEnabled(buttonName: String, enabled: Boolean)

  def priceForProduct(buttonName: String):Option[BigDecimal]
  /**
   * Prepares and realize the product to the user.
   * @param buttonName selected product identified by appropriate button.
   */
  def prepareAndSellProduct(buttonName: String)

  /**
   * Listener to hardware events
   */
  val hwEvent: Event => Unit
}
