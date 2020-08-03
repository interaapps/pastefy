<?php
namespace databases\migrate;

use modules\uloleorm\migrate\Migrate;

class PasteTable extends Migrate {
    public function database() {
        $this->create('paste', function($table) {
            $table->string("id", 8)->unique();
            $table->string("title");
            $table->string("content");
            $table->string("password");
            $table->string("folder");
            $table->int("userid");
            $table->int("encrypted");
            $table->date("created");
        });
    }
}
