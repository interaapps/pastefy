<?php
loadModule('ORM');
loadModule('ORM');
use uloleframework\ulole_modules\ORM\Table;
class TestTable extends Table {

    public $username, 
           $password;
    
    public function __construct() {
        $this->_table_name_ = "User";
    }

}
