<?php

namespace ulole\modules\Database;

class MySQLQuery {
    public $query, $con;
    function __construct($qu, $con) {
        $this->con = $con;
        $query = ($this->con)->query($qu);
        return $this;
    }

    function query($str) {
        ($this->con)->query($str);
        return $this;
    }

    function get() {
        //$forAll = ($this->query)->fetch_object();
        //while
    }

    function getObject() {
        return $this->query;
    }
}