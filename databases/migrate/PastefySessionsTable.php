<?php
namespace databases\migrate;

use modules\uloleorm\migrate\Migrate;

class PastefySessionsTable extends Migrate {
    public function database() {
        $this->create('pastefy_sessions', function($table) {
            $table->string("id", 8)->unique();
            $table->string("session_id");
            $table->int("userid");
            $table->string("user_key");
        });
    }
}
