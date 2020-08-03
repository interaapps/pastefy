<?php
namespace databases\migrate;

use modules\uloleorm\migrate\Migrate;

class PastefyAPITable extends Migrate {
    public function database() {
        $this->create('pastefy_apikeys', function($table) {
            $table->string("id", 35)->unique();
            $table->int("userid");
            $table->timestamp("created")->currentTimestamp();
        });
    }
}
