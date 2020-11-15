<?php
namespace databases;

use modules\uloleorm\Table;
class NotificationsTable extends Table {

    public $id,
           $user_id,
           $target_id,
           $paste,
           $created;
    
    public function database() {
        $this->_table_name_ = "pastefy_notifications";
        $this->__database__ = "main";
    }

}
