<?php

namespace ulole\modules\ORM;

class Selector {
    function andwhere($sel1, $operator, $sel2=null) {
        if ($sel2 == null) {
            $sel2 = $operator;
            $operator = "=";
        }

        $this->query .= ' AND '.$this->con->real_escape_string($sel1).''.$operator.'"'.$this->con->real_escape_string($sel2).'"';
        return $this;
    }

    function orwhere($sel1, $operator, $sel2=null) {
        if ($sel2 == null) {
            $sel2 = $operator;
            $operator = "=";
        }

        $this->query .= ' OR '.$this->con->real_escape_string($sel1).''.$operator.'"'.$this->con->real_escape_string($sel2).'"';
        return $this;
    }

    function where($sel1, $operator, $sel2=null) {
        if ($sel2 == null) {
            $sel2 = $operator;
            $operator = "=";
        }

        $this->query .= ' WHERE '.$this->con->real_escape_string($sel1).''.$operator.'"'.$this->con->real_escape_string($sel2).'"';
        return $this;
    }

    function run() {
        return $this->con->query($this->query.';');
    } 

}