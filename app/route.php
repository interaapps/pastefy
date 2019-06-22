<?php
/*

"/"          =   Homepage
"@__404__@"  =   Page not found

(Do not use duplicated keys!)

*/

// Directory for the views
$views_dir      =  "resources/views/";
$templates_dir  =  "resources/views/templates/";

$route = [
  "/"                        =>     "homepage.php",
  "/about"                   =>     "!AboutController@about", // Executing the static about function in the AboutController class
  "/d/([a-z]*)"              =>     "customtest.php",
  "@__404__@"                =>     "404.php"
];
