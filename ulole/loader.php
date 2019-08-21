<?php
/*
 *   Usage: loadCore()
 *   Description: Just loads the core scripts
 *
 */

spl_autoload_register(function($class) {
    @include_once "./".str_replace("\\","/",$class).".php";
});

//   Autoloading for app/controller   //
spl_autoload_register(function($class) {
    @include_once "./app/controller/".str_replace("\\","/",$class).".php";
});



function loadCore() {
    require "ulole/core/Router.php";
    require "ulole/core/Init.php";
}