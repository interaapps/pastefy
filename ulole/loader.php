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
    require "ulole/Core/Router.php";
    require "ulole/Core/Init.php";
}



/*
    Usage: loadModule( MODULENAME );

    @return uloleframework\Core\Classes\ModuleLoader
*/ 

function loadModule($name) {
    require_once "ulole/Core/Classes/ModuleLoader.php";
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/ulole_modules/");
    $load->load();
}

function loadUserModule($name) {
    require_once "ulole/Core/Classes/ModuleLoader.php";
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/user_modules/");
    $load->load();
}

function loadUlole($load) {
    require_once "ulole/".$load.".php";
}

function loadDB($db) {
    require_once "databases/".$db.".php";
}