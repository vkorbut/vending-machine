package com.github.vkorbut.fctryout

import com.github.vkorbut.fctryout.hardware.simulation.ConsoleAppHardwareSimulation
import com.github.vkorbut.fctryout.hardware.{Event, MachineHardwareInterface}

import scala.annotation.tailrec
import scala.io.StdIn

/**
 * @author Vladimir Korbut v.e.korbut@gmail.com
 */

trait MachineRuntime extends MachineHardwareInterface {
  //TODO: randomly generated product prices
  val productPrices: Map[String, BigDecimal] = buttonNames.zipWithIndex.map(v => (v._1, BigDecimal(v._2 % 3) / 2)).toMap
  val hwEvent = onEvent _

  override def priceForProduct(name:String) = productPrices.get(name)

  var currentState:State = ProductSelection(0, None)
  val actionsContainer = new Actions(this)

  def onEvent(event: Event): Unit = {
    currentState = actionsContainer.actions
      .find(_.isDefinedAt((currentState, event)))
      .map(_.apply(currentState, event))
      .getOrElse(currentState)
  }
}

object Main extends MachineRuntime with ConsoleAppHardwareSimulation{

  @tailrec
  private def readConsoleCommand(): Unit ={
    val line: String = StdIn.readLine().trim
    if (line != "exit"){
      cycle(line)
      readConsoleCommand()
    }
  }

  def main(args:Array[String]){
    printState()
    readConsoleCommand()
  }

  override def printState(): Unit = {
    val (enabled, disabled) = buttons.toList.partition(_._2._1)
    println("Enabled Buttons:")
    println(enabled.map{case (name, (_,true)) => s"($name)" case (name,_) => name}.mkString(" "))
    println("Disabled Buttons:")
    println(disabled.mkString(" "))
    println(s"Cash acceptor enabled: $cashAcceptorEnabled")
  }
}
