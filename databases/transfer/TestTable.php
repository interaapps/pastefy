<?php
namespace databases;

/*
    This is not working right now!
            Comming soon!
*/


use ulole\modules\ORM\Transfer;
class TestTable extends Transfer {
    public function database() {
        $this::create('User', function($table) {
            $table->id("id");
            $table->string("username");
            $table->string("password");
        });
    }
}
