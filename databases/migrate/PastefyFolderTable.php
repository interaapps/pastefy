<?php
namespace databases\migrate;

use modules\uloleorm\migrate\Migrate;

class PastefyFolderTable extends Migrate {
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
