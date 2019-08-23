<?php
namespace databases\migrate;

use ulole\modules\ORM\migrate\Migrate;
/*
    ULOLE MIGRATIONS WILL BE ADDED SOON!
*/
class Pastefy_folderTable extends Migrate {
    public function database() {
        $this->create('pastefy_folder', function($table) {
            $table->string("id", 8)->unique();
            $table->string("title");
            $table->int("userid");
            $table->string("parent");
            $table->date("created");
        });
    }
}
