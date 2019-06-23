<?php

namespace uloleframework\ulole_modules\ORM;

class Table {
    function save() {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        $query_keys = "";
        $query_values = "";
        foreach ($this as $a=>$b) {
            if ($a !== "_table_name_")
                if ($query_values=="") {
                    $query_values .= "'".$b."'";
                    $query_keys .= "`".$a."`";
                } else {
                    $query_values .= ", '".$b."'";
                    $query_keys .= ", `".$a."`";
                }
        }
        $query = "INSERT INTO `".$this->_table_name_."` (".$query_keys.") VALUES (".$query_values.");";
        $con->query($query);
    }

    function select($select = "*") {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        return (new Select($this, $select, $MYSQL_DATABASE_CONNECTION));
    }
}