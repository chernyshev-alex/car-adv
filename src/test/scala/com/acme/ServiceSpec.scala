package com.acme

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import MediaTypes._
import spray.json._
import DefaultJsonProtocol._  

class ApiServiceSpec extends Specification with Specs2RouteTest with ServiceAPI  {

    import ModelJsonProtocol._

    def actorRefFactory = system

    DbSetup.initialize()
    
    "service" should {
      
      "play to pong" in {
         Get("/api/v1/ping") ~> route ~> check {
           status should be equalTo (StatusCodes.OK)
           responseAs[String] must contain("pong")           
         }
      }

      "post and update car info" in {
        
         val ca = CarAdv("mazda", "GZ", 120000, true, 0)
         
         Post("/api/v1/cars", HttpEntity(`application/json`, ca.toJson.toString)) ~> route ~> check {
           
            status should be equalTo (StatusCodes.OK)
            
            val created = responseAs[String].parseJson.convertTo[CarAdv] 
            created.id should not be equalTo(-1)
            
            val changed = ca.copy(id = created.id, price = 100)
            
            Put("/api/v1/cars", HttpEntity(`application/json`, changed.toJson.toString)) ~> route ~> check {
              
              status should be equalTo (StatusCodes.OK)
              
              val updated = responseAs[String].parseJson.convertTo[CarAdv] 
              (created.id should be equalTo(updated.id)) and (updated.price should not be equalTo(created.price))
            }
         }
      }
      
      "find existing car info by id" in {

        val ca = CarAdv("bmv", "GZ", 0, true, 0)
        Post("/api/v1/cars", HttpEntity(`application/json`, ca.toJson.toString)) ~> route ~> check {
           
          status should be equalTo (StatusCodes.OK)
          
          val created = responseAs[String].parseJson.convertTo[CarAdv] 
        
          Get(s"/api/v1/cars/${created.id}") ~> route ~> check {
            
            status should be equalTo (StatusCodes.OK)
            
            val resp = responseAs[String].parseJson.convertTo[CarAdv] 
            (resp.id should be_>=(0L)) and (resp.title must be_==("bmv"))
         }
        }
      }

      "find not existing car gives response not found" in {
          Get("/api/v1/cars/9999999") ~> route ~> check {
            status should be equalTo (StatusCodes.NotFound)
         }
      }

      "find existing car" in {
         val ca = CarAdv("bmv", "GZ", 0, true, 0)
         Post("/api/v1/cars", HttpEntity(`application/json`, ca.toJson.toString)) ~> route ~> check {
           val created = responseAs[String].parseJson.convertTo[CarAdv] 
           Get(s"/api/v1/cars/${created.id}") ~> route ~> check {
              status should be equalTo (StatusCodes.OK)
              val found = responseAs[String].parseJson.convertTo[CarAdv] 
              found.id should be equalTo(created.id)
           }
         }
      }

      "delete not existing car gives response not found" in {
          Delete("/api/v1/cars/9999999") ~> route ~> check {
            status should be equalTo (StatusCodes.NotFound)
         }
      }
      
      "delete existing car" in {
         val ca = CarAdv("bmv", "GZ", 0, true, 0)
         Post("/api/v1/cars", HttpEntity(`application/json`, ca.toJson.toString)) ~> route ~> check {
           val created = responseAs[String].parseJson.convertTo[CarAdv] 
           Delete(s"/api/v1/cars/${created.id}") ~> route ~> check {
               status should be equalTo (StatusCodes.OK)
               Get(s"/api/v1/cars/${created.id}") ~> route ~> check {
                  status should be equalTo (StatusCodes.NotFound)
               }
           }
         }
      }

      "list cars info with pagesize, offset, ordered by price" in {
        
         for (price <- 10 to 20) { 
           val ca = CarAdv(s"bmv/${price}", "GZ", price, true, 0)
           Post("/api/v1/cars", HttpEntity(`application/json`, ca.toJson.toString)) ~> route
         }
        
         Get("/api/v1/cars?sortby=price&limit=2&offset=4") ~> route ~> check {
            status should be equalTo (StatusCodes.OK)
            
            val ls = responseAs[String].parseJson.convertTo[List[CarAdv]] 
            ls.size should be_==(2)
            ls(0).price should be_<=(ls(1).price)
         }
      }
      
      "list cars info" in {
         Get("/api/v1/cars") ~> route ~> check {
            status should be equalTo (StatusCodes.OK)
            val ls = responseAs[String].parseJson.convertTo[List[CarAdv]] 
            ls.size should be >(0)
         }
      }
    }
}
