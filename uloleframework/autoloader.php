<?php
/*
    Usage: loadCore()
    Description: Just loads the core scripts
*/
function loadCore() {
    require "uloleframework/Core/Router.php";
    require "uloleframework/Core/Init.php";
}

/*
    Usage: loadModule( MODULENAME );

    @return uloleframework\Core\Classes\ModuleLoader
*/ 

function loadModule($name) {
    require_once "uloleframework/Core/Classes/ModuleLoader.php";
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/ulole_modules/");
    $load->load();
}

function loadUserModule($name) {
    require_once "uloleframework/Core/Classes/ModuleLoader.php";
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/user_modules/");
    $load->load();
}

function loadUlole($load) {
    require_once "uloleframework/".$load.".php";
}

function loadDB($db) {
    require_once "databases/".$db.".php";
}