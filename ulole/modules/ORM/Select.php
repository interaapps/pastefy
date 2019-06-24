<?php

namespace ulole\modules\ORM;

class Select {
    public $that,
           $query,
           $con;
    function __construct($that, $select, $con) {
        $this->that  = $that;
        $this->con   = $con->getObject();
        $this->query = 'SELECT '.$select.'FROM '.$this->that->_table_name_;
    }

    function where($sel1, $operator, $sel2=null) {
        if ($sel2 == null) {
            $sel2 = $operator;
            $operator = "=";
        }

        $this->query .= ' '.$sel1.''.$operator.'"'.$sel2.'"';
        return $this;
    } // $this->query

    function limit($limit) {
        $this->query .= 'LIMIT '.$limit;
        return $this;
    }

    function order($order) {
        $this->query .= 'ORDER BY '.$order;
        return $this;
    }

    function get() {
        $qu = $this->con->query($this->query.';');
        $outArray = [];
        while($obj=$qu->fetch_object()) {
            array_push($outArray, $obj);
        }
        return $outArray;
    }
}