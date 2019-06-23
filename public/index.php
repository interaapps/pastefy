<?php

//*               DEVERM-ROUTER 2.0
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);// */
chdir('..');

// INIT ROUTE
require "app/route.php";


// IMPORTING STANDARD LIBS
require "uloleframework/autoloader.php";

loadCore();
// Autoloads the controllers
if (isset($config->settings->autoload))
    foreach ( $config->settings->autoload as $import )
        Router::autoload($import);

// Initializing routings
$router = new Router($views_dir, $templates_dir);
$router->set($route);
$router->route();
