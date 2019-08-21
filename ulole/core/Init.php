<?php

/*
    Init.php
    Inititializing configs and more
*/

global $config, $config_env;

$config = json_decode(file_get_contents("conf.json"));

\ulole\core\classes\Lang::setLang((isset($config->options->defaultlang)) ? $config->options->defaultlang : "en");

if ((isset($config->options->detectlanguage) ? $config->options->detectlanguage : false)) {
    if (\file_exists("resources/languages/".substr($_SERVER['HTTP_ACCEPT_LANGUAGE'], 0, 2).".json"))
    \ulole\core\classes\Lang::setLang(substr($_SERVER['HTTP_ACCEPT_LANGUAGE'], 0, 2));
}

$config_env = "";
if (file_exists("env.json")) {
    $config_env = json_decode(file_get_contents("env.json"));

    if (isset($config_env->MySQL->database) && (isset($config->options->use_mysql))?$config->options->use_mysql:false ) {
        global $MYSQL_DATABASE_CONNECTION;
        $MYSQL_DATABASE_CONNECTION = new ulole\modules\Database\MySQL(
            $config_env->MySQL->username,
            $config_env->MySQL->password,
            $config_env->MySQL->database,
            $config_env->MySQL->server,
            $config_env->MySQL->port
        );
    
        if (!$MYSQL_DATABASE_CONNECTION) 
            unset($MYSQL_DATABASE_CONNECTION);
    }
}

