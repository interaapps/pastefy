<?php

namespace ulole\modules\ORM;

class Update extends Selector {
    public $that,
           $query,
           $con;
    function __construct($that, $select, $con) {
        $this->that  = $that;
        $this->con   = $con->getObject();
        $this->query = 'UPDATE '.$this->that->_table_name_;
    }

    function set($set, $to) {
        $this->query .= ' SET '.$this->con->real_escape_string($set).'="'.$this->con->real_escape_string($to).'"';
        return $this;
    }

    function andset($set, $to) {
        $this->query .= ' , '.$this->con->real_escape_string($set).'="'.$this->con->real_escape_string($to).'"';
        return $this;
    }

    function byVars() {
        $outputString = "";
        foreach ($this->that as $key=>$value) {
            if ($key !== "_table_name_") {
                if (!isset($value)) echo ":: $key is NOT SET!\n"; else echo "$key is set!";
                if (isset($value)) {
                    if ($outputString == "") {
                        $outputString .= ' SET ';
                        $outputString .= '`'.$this->con->real_escape_string($key).'`="'.$this->con->real_escape_string($value).'" ';
                    } else {
                        $outputString .= ', `'.$this->con->real_escape_string($key).'`="'.$this->con->real_escape_string($value).'" ';
                    }
                }
            }
        }

        $this->query .= $outputString;
        return $this;
    }


}