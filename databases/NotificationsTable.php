<?php
namespace databases;

use modules\uloleorm\Table;
class NotificationsTable extends Table {

    public $id,
           $message,
           $user_id,
           $received,
           $already_read,
           $url,
           $created;
    
    public function database() {
        $this->_table_name_ = "pastefy_notifications";
        $this->__database__ = "main";
    }
}
