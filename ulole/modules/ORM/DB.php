<?php
namespace ulole\modules\ORM\Table;

class DB extends Table {
    
    public function __construct($name) {
        $this->_table_name_ = $name;
    }

}
