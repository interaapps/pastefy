<?php

namespace ulole\modules\ORM;

class Delete extends Selector {
    public $that,
           $query,
           $con;
    function __construct($that, $select, $con) {
        $this->that  = $that;
        $this->con   = $con->getObject();
        $this->query = 'DELETE FROM '.$this->that->_table_name_;
    }

  
    function limit($limit) {
        $this->query .= 'LIMIT '.$limit;
        return $this;
    }

    function order($order) {
        $this->query .= 'ORDER BY '.$order;
        return $this;
    }


}