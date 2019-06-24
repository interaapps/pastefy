<?php
// loadModule('ORM');

use modules\ORM\Table;
class TestTable extends Table {

    public $username, 
           $password;
    
    public function __construct() {
        $this->_table_name_ = "User";
    }

}
