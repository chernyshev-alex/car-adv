package com.acme

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scalikejdbc._
import scalikejdbc.config._

object Bootstrap extends App {

  DbSetup.initialize()

  implicit val ac = ActorSystem("cars")

  val system = ac.actorOf(Props[ServiceActor], "cars-adv-api")

  implicit val timeout = Timeout(1.seconds)
  
  IO(Http) ? Http.Bind(system, interface = "127.0.0.1", port = 8080)
}