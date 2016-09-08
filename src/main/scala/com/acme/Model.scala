package com.acme

import scalikejdbc._
import org.joda.time.LocalDate
import spray.json._
import DefaultJsonProtocol._  

/**
* Car adverts should have the following fields:
* **id** (_required_): **int** or **guid**, choose whatever is more convenient for you;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_;
* **fuel** (_required_): gasoline or diesel, use some type which could be extended in the future by adding additional fuel types;
* **price** (_required_): **integer**;
* **new** (_required_): **boolean**, indicates if car is new or used;
* **mileage** (_only for used cars_): **integer**;
* **first registration** (_only for used cars_): **date** without time.
*/

/**
* Presents car advert info
*/
case class CarAdv(title : String, fuelType : String,
        price : Int, isNew : Boolean, milleage : Int, 
        regDate : LocalDate = LocalDate.now(), 
        var id : Long = -1)

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object CarAdvJsonFormat extends RootJsonFormat[CarAdv] {
     
     def write(o : CarAdv) = JsObject(
             "id"          -> JsNumber(o.id), 
             "title"       -> JsString(o.title), 
             "fueltype"    -> JsString(o.fuelType), 
             "price"       -> JsNumber(o.price), 
             "isnew"       -> JsBoolean(o.isNew), 
             "milleage"    -> JsNumber(o.milleage),
             "regdate"     -> JsString(o.regDate.toString))

     def read(js : JsValue) = { 
         js.asJsObject.getFields("id","title", "fueltype", "price", "isnew", "milleage", "regdate") match {
           case Seq(JsNumber(id), JsString(title), JsString(fueltype), 
               JsNumber(price), JsBoolean(isnew), JsNumber(milleage), JsString(regdate)) =>
                   CarAdv(title, fueltype, price.toIntExact, isnew, milleage.toIntExact, 
                       LocalDate.parse(regdate), id.toLongExact)
         }
     }
   }
}
  
object CarAdv extends SQLSyntaxSupport[CarAdv] {
  
  override val tableName = "cars_adv"
  val col = CarAdv.column
  val ca  = CarAdv.syntax("ca")

  def apply(rs : WrappedResultSet) : CarAdv = new CarAdv(
      rs.string("title"), 
      rs.string("fuel_type"), 
      rs.int("price"), 
      rs.boolean("is_new"), 
      rs.int("milleage"), 
      rs.jodaLocalDate("reg_date"),
      rs.int("id"))
  
  /**
   * create entity and return auto-seq primary key
   */
  def create(ca : CarAdv)(implicit ss : DBSession = AutoSession) : CarAdv = {
    val id = withSQL {
      insertInto(CarAdv).namedValues(
          col.fuelType -> ca.fuelType,
          col.isNew -> ca.isNew,
          col.milleage -> ca.milleage,
          col.price -> ca.price,
          col.title -> ca.title,
          col.regDate -> ca.regDate)
    }.updateAndReturnGeneratedKey.apply() 
    ca.id = id
    ca
  }

  /**
   * returns 0/1 - not found/updated entry
   */
  def update(ca : CarAdv)(implicit ss : DBSession = AutoSession) : Long = {
    withSQL {
      QueryDSL.update(CarAdv).set(
          col.fuelType -> ca.fuelType,
          col.isNew -> ca.isNew,
          col.milleage -> ca.milleage,
          col.price -> ca.price,
          col.title -> ca.title,
          col.regDate -> ca.regDate).where.eq(col.id, ca.id)
    }.update.apply() 
  }
  
  /**
   * returns 0/1 - not found/deleted entry
   */
  def delete(id : Long)(implicit ss : DBSession = AutoSession) : Long = {
    withSQL { deleteFrom(CarAdv).where.eq(CarAdv.column.id, id) }.update.apply()
  }
  
  def list(offset : Int=0, limit : Int=100, ord : String = "title")(implicit ss : DBSession = AutoSession) : List[CarAdv] = {
    withSQL { select.from(CarAdv as ca).orderBy(ca.column(ord)).asc
          .limit(scala.math.min(limit, 100)).offset(offset) }.map(rs => CarAdv(rs)).list.apply()
  }
  
  def findById(id : Long)(implicit ss : DBSession = AutoSession) : Option[CarAdv] = {
    withSQL { select.from(CarAdv as ca).where.eq(ca.id, id) }.map(rs => CarAdv(rs)).single.apply()
  }

}