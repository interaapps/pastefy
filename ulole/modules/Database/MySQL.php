<?php

namespace ulole\modules\Database;

class MySQL {
    public $con;
    function __construct($username, $password=false, $database=false,$host=false,$port=3306) {
        if ( ($password === false) && ($database === false) && ($host === false) ) {
            $db_username  =  $username["username"];
            $db_password  =  $username["password"];
            $db_database  =  $username["database"];
            $db_host      =  $username["server"];
        } else {
            $db_username  =  $username;
            $db_password  =  $password;
            $db_database  =  $database;
            $db_host      =  $host;
        }
        $this->con = new \mysqli($db_host, $db_username, $db_password, $db_database, $port);
    }

    function query($str) {
        require uloleframework/Database/MySQLQuery.php;
        return (new uloleframework\Core\Database\MySQLQuery($str, $this->con));
    }

    function getObject() {
        return $this->con;
    }
}