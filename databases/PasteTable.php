<?php
namespace databases;

use ulole\modules\ORM\Table;
class PasteTable extends Table {

    public $id, // INSERT YOUR ROWS IN HERE 
           $title,
           $content,
           $created,
           $password,
           $userid,
           $encrypted,
           $folder;
    
    public function __construct() {
        $this->_table_name_ = "paste";
    }

}
