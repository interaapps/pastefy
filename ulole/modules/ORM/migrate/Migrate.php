<?php

namespace ulole\modules\ORM\migrate;

/**
 * Migrates Databases Schemas
 * 
 * @author JulianFun123
 */
class Migrate {
    
    public function database() {
        throw new Exception("database() method is unset!");
    }

    public function create(String $tableName, $function) {
        $migrationObject = new MigrationObjects;
        $function($migrationObject);
        //echo json_encode($migrationObject->queryArray);
        
        $query = "CREATE TABLE IF NOT EXISTS `".$tableName."` (";
        $comma = "";
        foreach($migrationObject->queryArray as $name=>$obj) {
                
            $query .= $comma."`".$name."` ".$obj->type
                   .(($obj->length!==false)?"(".$obj->length.")":"")
                   .(($obj->ai)?" AUTO_INCREMENT":"")

                   .(($obj->default!==null)?" DEFAULT ".$obj->default:"")
                   .(($obj->defaultNull) ? " DEFAULT NULL" : "")
                   
                   .(($obj->ai)?" , ADD PRIMARY KEY (`".$name."`)":"");
            $query .= "\n";
            $comma = ",";
        }
        
        $query .= ") ENGINE=INNODB;";
        echo $query;
    }
}