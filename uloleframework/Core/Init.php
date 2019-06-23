<?php

/*
    Init.php
    Inititializing configs and more
*/

global $config;

$config = json_decode(file_get_contents("conf.json"));

$config_env = "";
if (file_exists("env.json"))
    $config_env = json_decode(file_get_contents("env.json"));

