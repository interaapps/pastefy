<?php
/*
    Usage: loadCore()
    Description: Just loads the core scripts
*/
function loadCore() {
    require "uloleframework/Core/Router.php";
    require "uloleframework/Core/Init.php";
}
require "uloleframework/Core/Classes/ModuleLoader.php";
/*
    Usage: loadModule( MODULENAME );

    @return uloleframework\Core\Classes\ModuleLoader
*/ 

function loadModule($name) {
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/ulole_modules/");
    $load->load();
}

function loadUserModule($name) {
    require "uloleframework/Core/Classes/ModuleLoader.php";
    $load = new uloleframework\Core\Classes\ModuleLoader($name, "uloleframework/user_modules/");
    $load->load();
}

function loadUlole($load) {
    require "uloleframework/".$load.".php";
}

function loadDB($db) {
    require "databases/".$db.".php";
}