<?php
/*

"/"          =   Homepage
"@__404__@"  =   Page not found

(Do not use duplicated keys!)

You can use also this syntax for adding pages
$router->get("/test1", "homepage");
*/

// Directory for the views
$views_dir      =  "resources/views/";
$templates_dir  =  "resources/views/templates/";

$route = [
  "@__404__@"                =>     "404.php"
];

$router->get("/", "homepage.php");

$router->get("/folder/", "404.php");
$router->get("/pasteList", "!PasteController@pasteList");

$router->get("/user:login", "!UserController@login");

$router->post("/create:folder", "!FolderController@createFolder");
$router->post("/create:paste", "!PasteController@createPaste");

$router->post("/get:folder", "!FolderController@getMyDirectories");

$router->get("/([a-zA-Z0-9]*)", "!PasteController@openPaste");
$router->get("/p/([a-zA-Z0-9]*)", "!PasteController@openPaste");

$router->get("/folder/([a-zA-Z0-9]*)", "!FolderController@folder");

$router->get("/p/login/([a-zA-Z0-9]*)", "!PasteController@password");
$router->get("/p/raw/([a-zA-Z0-9]*)", "!PasteController@rawPaste");
$router->get("/raw/([a-zA-Z0-9]*)", "!PasteController@rawPaste");
$router->get("/([a-zA-Z0-9]*)/raw", "!PasteController@rawPaste");

$router->get("/delete:folder/([a-zA-Z0-9]*)", "!DeleteController@deleteFolder");
$router->get("/delete:paste/([a-zA-Z0-9]*)", "!DeleteController@deletePaste");