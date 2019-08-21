<?php
namespace databases\migrate;

use ulole\modules\ORM\migrate\Migrate;

class MessagesTable extends Migrate {
    public function database() {
        $this->create('messages', function($table) {
            $table->int("id")->ai();
            $table->string("username");
            $table->string("password", 255);
            $table->enum("enumTest", ["val1","val2"]);
        });
    }
}
