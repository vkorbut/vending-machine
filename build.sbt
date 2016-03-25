name := "vladimir-korbut-challenge"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in (Compile, run) := Some ("com.github.vkorbut.fctryout.Main")

mainClass in (Compile, packageBin) := Some("com.github.vkorbut.fctryout.Main")