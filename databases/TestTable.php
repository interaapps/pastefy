<?php
loadModule('ORM');
use uloleframework\ulole_modules\ORM\Table;
class TestTable extends Table {

    public $username, 
           $password;
    
           
    public $_table_name_;
    public function __construct() {
        $this->_table_name_ = "User";
    }

}
