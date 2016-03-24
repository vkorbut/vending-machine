package com.github.vkorbut.fctryout

import com.github.vkorbut.fctryout.hardware.simulation.ConsoleAppHardwareSimulation

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */

object Main {

  val products: Seq[(String, BigDecimal)] = Seq[(String, BigDecimal)](
  "americano" -> 1,
  "espresso"->1.25,
  "latte"->1.45,
  "cappuccino"->2)

  val hardware = new ConsoleAppHardwareSimulation(products.map(_._1))
  val runtime = new MachineRuntime(hardware, products.toMap)

  def main(args: Array[String]) {
    hardware.printState()
    hardware.processConsoleCommands()
  }

}
