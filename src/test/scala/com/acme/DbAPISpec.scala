package com.acme

//import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable._
import org.joda.time._
import scalikejdbc._

class DbApiSpec extends Specification {

  DbSetup.initialize()

  "car advert" should {
    
      "create entity" in { 
        val ca = CarAdv("mazda", "GZ", 120000, true, 0)
        ca.id should equalTo(-1) 
        CarAdv.create(ca)
        ca.id must be_!=(-1L)
      }

      "update entity" in { 
        val ca = CarAdv("mazda", "GZ", 120000, true, 0)
        CarAdv.create(ca)
        val changed = ca.copy(fuelType = "DS")
        
        CarAdv.update(changed) must be_==(1)
        
        val check = CarAdv.findById(ca.id)
        check.get.fuelType must be_== ("DS")
      }

      "delete by id" in { 
        val ca = CarAdv("mazda", "GZ", 120000, true, 0)
        CarAdv.create(ca)
        CarAdv.delete(ca.id)
        CarAdv.findById(ca.id) == None
      }
      
      "find by id" in { 
        val ca = CarAdv("mazda", "GZ", 120000, true, 0)
        CarAdv.create(ca)
        val check = CarAdv.findById(ca.id)
        (ca.title === check.get.title)
      }
      
      "list with pagination" in {
        for (i <- 0 to 10) {
          CarAdv.create(CarAdv("mazda", "GZ", i, true, 0))
        }
        var ls = CarAdv.list(0, 4)
        ls.size must be_==(4)
        ls = CarAdv.list(10000, 4)
        ls.size must be_==(0)
      }

      "list with sort by field" in {
        for (i <- 0 to 10) {
          CarAdv.create(CarAdv("mazda", "GZ", i, true, 0))
        }
        var ls = CarAdv.list(0, 2, "price")
        ls(0).price >= ls(1).price
      }
      
  } 
}
