<?php
namespace ulole\modules\ORM\migrate;

class DatabaseObject {

    public  $unique=false,
            $type,
            $fieldName,
            $length=false,
            $ai=false,
            $default=null,
            $defaultNull=false,
            $collate=false;

    public function __construct($fieldType, $fieldName) {
        $this->type = $fieldType;
        $this->fieldName = $fieldName;
    }

    public function default($defaultValue) {

        if ($defaultValue==null)
            $this->defaultNull = true;
        else
            $this->default = $defaultValue;
        return $this;

    }

    public function unique() {
        $this->unique = true;
        return $this;
    }

    public function ai() {
        $this->ai = true;
        return $this;
    }

    public function setEnum($keys) {
        $this->length = "";
        $comma = "";
        foreach($keys as $key){
            $this->length .= $comma."'".$key."'";
            $comma = ",";
        }
        return $this;
    }

}