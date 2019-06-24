# Class

The class have to be in the databases folder
```php
<?php
namespace databases;

use ulole\modules\ORM\Table;
class TestTable extends Table {

    public $username, 
           $password;
    
    public function __construct() {
        $this->_table_name_ = "User";
    }

}

```