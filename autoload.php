<?php
/* *******************************
    A simple autoload function
******************************* */

$uppmlock = [];
if (file_exists("uppm.locks.json"))
    $uppmlock = json_decode(file_get_contents("uppm.locks.json"));


spl_autoload_register(function($class) {
    global $uppmlock;
    if (isset($uppmlock->directnamespaces->{$class}))
        @include_once "./".str_replace("\\","/",$uppmlock->directnamespaces->{$class});
    else
        @include_once "./".str_replace("\\","/",$class).".php";
});

if (isset($uppmlock->initscripts))
    foreach ($uppmlock->initscripts as $script) {
        @include_once $script;
    }