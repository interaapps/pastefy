<!--You can delete this file, but not in usage of the testserver!!-->


<script>console.log("ULOLE TESTSERVER! DONT USE IT ON A PUBLIC SERVER");</script><?php if (preg_match("/\.(?:png|jpg|jpeg|gif|js|css|html)$/", $_SERVER["REQUEST_URI"])) {return false;}?><?php

//*               DEVERM-ROUTER 2.0
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);// */
chdir('..');

// INIT ROUTE
require "app/route.php";


// IMPORTING STANDARD LIBS
require "uloleframework/autoloader.php";


// Autoloads the controllers
Router::autoload("app/controller");

// Initializing routings
$router = new Router($views_dir, $templates_dir);
$router->set($route);
$router->route();
