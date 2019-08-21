<?php

namespace ulole\modules\ORM\migrate;

class MigrationObjects {
    public $queryArray = [];

    public function queryCombinder($obj) {
        $this->queryArray[$obj->fieldName] = $obj;
    }

    public function string(string $field, $length=false) {
        if ($length===false)
            $databaseObject = new DatabaseObject("TEXT", $field);
        else {
            $databaseObject = new DatabaseObject("VARCHAR", $field);
            $databaseObject->length = $length;
        }
        $this->queryCombinder($databaseObject);
        return $databaseObject;
    }

    public function varchar(string $field) {
        $databaseObject = new DatabaseObject("STRING", $field);
        $this->queryCombinder($databaseObject);
        return $databaseObject;
    }

    public function text(string $field) {
        $databaseObject = new DatabaseObject("TEXT", $field);
        $this->queryCombinder($databaseObject);
        return $databaseObject;
    }

    public function enum(string $field, $enums=[]) {
        $databaseObject = new DatabaseObject("ENUM", $field);
        $databaseObject->setEnum($enums);
        $this->queryCombinder($databaseObject);
        return $databaseObject;
    }

    public function int(string $field) {
        $databaseObject = new DatabaseObject("INT", $field);
        $this->queryCombinder($databaseObject);
        return $databaseObject;
    }

}