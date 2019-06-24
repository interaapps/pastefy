<?php

/*
    Init.php
    Inititializing configs and more
*/

global $config;

$config = json_decode(file_get_contents("conf.json"));

$config_env = "";
if (file_exists("env.json")) {
    $config_env = json_decode(file_get_contents("env.json"));

    if (isset($config_env->MySQL->database)) {
        global $MYSQL_DATABASE_CONNECTION;
        $MYSQL_DATABASE_CONNECTION= new ulole\modules\Database\MySQL(
            $config_env->MySQL->username,
            $config_env->MySQL->password,
            $config_env->MySQL->database,
            $config_env->MySQL->server,
            $config_env->MySQL->port
        );
    }
}

