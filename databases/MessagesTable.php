<?php
namespace databases;

use ulole\modules\ORM\Table;
class MessagesTable extends Table {

    public $row1, // INSERT YOUR ROWS IN HERE 
           $row2;
    
    public function __construct() {
        $this->_table_name_ = "messages";
    }

}
