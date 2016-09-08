package com.acme

import akka.actor.Actor
import spray.routing._
import spray.http._
import StatusCodes._
import spray.json._
import DefaultJsonProtocol._  
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
  
class ServiceActor extends Actor with ServiceAPI {
  
  def actorRefFactory = context
  def receive = runRoute(route)
}

// == Service API==================

/**
 * API 
 * 
 * get /ping = test healthy
 * 
 * get /cars?sortby={field=Id}&limit={100}&offset={0} - list cars with optional sort and pagination
 * 	where  sortby = sort field, default Id (opt)
 * 			   limit - page size, max 100 default 10  (opt)
 * 				 offset - scroll offset, default 0 (opt) 
 * get /cars/{id} - get car info
 * 
 * post /cars {json content} - create a new car from json
 *   curl -d "[data]" -H"Content-Type: application/json" -X POST localhost:8080/api/v1/cars
 *   
 * put /cars/{Id} {json content} - update car info from json 
 *   curl -d "[data]" -H"Content-Type: application/json" -X PUT localhost:8080/api/v1/cars
 * 
 * delete /cars/{id}  - delete car 
 *   curl -X DELETE localhost:8080/api/v1/cars/{carId}
 *  
 */
trait ServiceAPI extends HttpService {

    val API_VER   = "v1"
    val API_PREFIX = "api" / API_VER

    val PATH_CARS = "cars" 

    import ModelJsonProtocol._
    
    val ping = path("ping") { complete("pong") } 

    val listCars = path(PATH_CARS) {
      parameters('sortby ? "title", 'limit.as[Int] ? 10, 'offset.as[Int] ? 0) { (sortBy, limit, offset) =>
          val ls = CarAdv.list(offset, limit, sortBy)
          complete(ls.toJson.toString())
      }
    } 
    
    val findCarById = path(PATH_CARS / IntNumber) { id =>
          CarAdv.findById(id) match {
            case Some(o) => complete(o.toJson.toString())
            case None => complete(StatusCodes.NotFound)
          }
    } 
    
    val createCar = path(PATH_CARS) { entity(as[CarAdv]) { ca => 
        CarAdv.create(ca)
        complete(ca.toJson.toString()) 
      }
    }
    
    val updateCar = path(PATH_CARS) { entity(as[CarAdv]) { ca =>  
        if (CarAdv.update(ca) ==0) complete(StatusCodes.NotFound) else complete(ca.toJson.toString()) 
      } 
    }
    
    val deleteCar = path(PATH_CARS / IntNumber) { id => 
        if (CarAdv.delete(id) ==0) complete(StatusCodes.NotFound) else complete(id.toJson.toString)
    }
    
    val route = pathPrefix(API_PREFIX) { 
        get {
          ping ~ listCars ~ findCarById
        } ~
        post {
          createCar
        } ~
        put {
          updateCar
        } ~
        delete {
          deleteCar
        }
      } 
}