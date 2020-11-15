<?php
namespace databases;

use modules\uloleorm\Table;
class SharedPastesTable extends Table {

    public $id,
        $user_id,
        $target_id,
        $paste,
        $created;
    
    public function database() {
        $this->_table_name_ = "pastefy_shared_pastes";
        $this->__database__ = "main";
    }

}
