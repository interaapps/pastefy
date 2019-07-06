<?php

namespace ulole\modules\ORM;

class Table {
public $_table_name_;

    function save() {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        $query_keys = "";
        $query_values = "";
        foreach ($this as $a=>$b) {
            if ($a !== "_table_name_")
                if ($query_values=="") {
                    $query_values .= "'".$con->real_escape_string($b)."'";
                    $query_keys .= "`".$a."`";
                } else {
                    $query_values .= ", '".$con->real_escape_string($b)."'";
                    $query_keys .= ", `".$a."`";
                }
        }
        $query = "INSERT INTO `".$this->_table_name_."` (".$query_keys.") VALUES (".$query_values.");";
        $con->query($query);
    }

    function getObject() {
        global $MYSQL_DATABASE_CONNECTION;
        return $MYSQL_DATABASE_CONNECTION->getObject();
    }

    function query($query) {
        global $MYSQL_DATABASE_CONNECTION;
        return $MYSQL_DATABASE_CONNECTION->getObject()->query($query);
    }

    
    function select($select = "*") {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        return (new Select($this, $select, $MYSQL_DATABASE_CONNECTION));
    }

    function delete($select = "*") {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        return (new Delete($this, $select, $MYSQL_DATABASE_CONNECTION));
    }

    function update($select = "*") {
        global $MYSQL_DATABASE_CONNECTION;
        $con = $MYSQL_DATABASE_CONNECTION->getObject();
        return (new Update($this, $select, $MYSQL_DATABASE_CONNECTION));
    }
}