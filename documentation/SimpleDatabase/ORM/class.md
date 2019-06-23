# Class

The class have to be in the databases folder
```php
<?php
loadModule('ORM');
use uloleframework\ulole_modules\ORM\Table;
class TestTable extends Table /* You have to extend Table to use the ORM methods */ {

    // Here you have to insert the Table rows
    public $username, 
           $password;
    
    // Here you have to give the table name
    public $_table_name_;
    public function __construct() {
        $this->_table_name_ = "User";
    }

}

```