<?php
namespace databases;

use ulole\modules\ORM\Table;
class PasteFolderTable extends Table {

    public $id, // INSERT YOUR ROWS IN HERE 
           $name,
           $userid,
           $parent,
           $created;
    
    public function __construct() {
        $this->_table_name_ = "pastefy_folder";
    }

}
