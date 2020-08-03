<?php
namespace databases;

use modules\uloleorm\Table;
class PastefyAPITable extends Table {

    public $id,
           $userid,
           $created;
    
    public function database() {
        $this->_table_name_ = "pastefy_apikeys";
        $this->__database__ = "main";
    }

}
