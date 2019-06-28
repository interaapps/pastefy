<?php

namespace ulole\modules\ORM;

class Transfer {
    public $table;
    function create($table, $func) {

        $this->table = $table;

        $func();

    }
}