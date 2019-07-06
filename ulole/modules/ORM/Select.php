<?php

namespace ulole\modules\ORM;

class Select extends Selector {
    public $that,
           $query,
           $con;
    function __construct($that, $select, $con) {
        $this->that  = $that;
        $this->con   = $con->getObject();
        $this->query = 'SELECT '.$select.' FROM '.$this->that->_table_name_;
    }

  
    function limit($limit) {
        $this->query .= 'LIMIT '.$limit;
        return $this;
    }

    function order($order) {
        $this->query .= 'ORDER BY '.$order;
        return $this;
    }


    function get() { //echo $this->query.';';
        $qu = $this->con->query($this->query.';');
        $outArray = [];
        while($obj=$qu->fetch_object()) {
            array_push($outArray, $obj);
        }
        return $outArray;
    }

    function first() { //echo $this->query.';';
        $qu = $this->con->query($this->query.';');
        while($obj=$qu->fetch_object())
            $out = $obj;
        return $out;
    }
}