package com.acme

import scala.util.{Try, Success, Failure}
import scalikejdbc.config._
import scalikejdbc._

object DbSetup {

  var isInitialized = false 

  DBs.setup('default)
  
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true,
    singleLineMode = true,
    printUnprocessedStackTrace = false,
    stackTraceDepth= 10,
    logLevel = 'warn,
    warningEnabled = false,
    warningThresholdMillis = 3000L,
    warningLogLevel = 'warn )

  
  def initialize(force : Boolean = false) = {
    if (!isInitialized || force) {
      genTables()
    }
    isInitialized = true
  }

  private def genTables() : Unit = {
    DB autoCommit { implicit ss => 
      sql"""
        create sequence cars_adv_seq start with 1;
        create table cars_adv ( 
           id bigint not null default nextval('cars_adv_seq') primary key, 
           title      varchar(255),
           fuel_type   char(2),
           price      integer not null,
           is_new     boolean default false,
           milleage   integer not null,
           reg_date   date not null);
      """.execute.apply()
    }
  }
  
}