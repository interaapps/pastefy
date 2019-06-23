<?php

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);// */
chdir('..');


// IMPORTING STANDARD LIBS
require "uloleframework/autoloader.php";

loadCore();
// Autoloads the controllers
if (isset($config->settings->autoload))
    foreach ( $config->settings->autoload as $import )
        Router::autoload($import);

// Initializing routings
$router = new Router();
require "app/route.php";
$router->setDirectories($views_dir, $templates_dir);
$router->set($route);
$router->route();