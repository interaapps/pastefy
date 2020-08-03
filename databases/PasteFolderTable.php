<?php
namespace databases;

use modules\uloleorm\Table;
class PasteFolderTable extends Table {

    public $id, // INSERT YOUR ROWS IN HERE 
           $name,
           $userid,
           $parent,
           $created;
    
    public function database() {
        $this->_table_name_ = "pastefy_folder";
        $this->__database__ = "main";
    }

}
