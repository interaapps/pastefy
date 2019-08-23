<?php
namespace databases\migrate;

use ulole\modules\ORM\migrate\Migrate;
/*
    ULOLE MIGRATIONS WILL BE ADDED SOON!
*/
class Pastefy_sessionsTable extends Migrate {
    public function database() {
        $this->create('pastefy_sessions', function($table) {
            $table->string("id", 8)->unique();
            $table->string("session_id");
            $table->int("userid");
            $table->string("user_key");
        });
    }
}
