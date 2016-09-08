
@author  chernyshev.alexander@gmail.com

 Prerequisites 
    
   scala 2.11.8
 
   spray 1.3.3

   akka  2.3.9

   h2 database 1.4.192


 Run

	sbt run
 	http://localhost:8080/api/v1/ping

 Test

 	sbt test  	- test all (persistent model and service)


======================================================================
### Requirements


Car adverts should have the following fields:
* **id** (_required_): **int** or **guid**, choose whatever is more convenient for you;
* **title** (_required_): **string**, e.g. _"Audi A4 Avant"_;
* **fuel** (_required_): gasoline or diesel, use some type which could be extended in the future by adding additional fuel types;
* **price** (_required_): **integer**;
* **new** (_required_): **boolean**, indicates if car is new or used;
* **mileage** (_only for used cars_): **integer**;
* **first registration** (_only for used cars_): **date** without time.

Service should:
* have functionality to return list of all car adverts;
  * optional sorting by any field specified by query parameter, default sorting - by **id**;
* have functionality to return data for single car advert by id;
* have functionality to add car advert;
* have functionality to modify car advert by id;
* have functionality to delete car advert by id;
* have validation (see required fields and fields only for used cars);
* accept and return data in JSON format, use standard JSON date format for the **first registration** field.

### Additional requirements

* Service should be able to handle CORS requests from any domain.
* Think about test pyramid and write unit-, integration- and acceptance-tests if needed.
* It's not necessary to document your code, but having a readme.md for developers who will potentially use your service would be great.
