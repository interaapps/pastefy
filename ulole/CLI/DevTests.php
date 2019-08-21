<?php
require "ulole/CLI/Custom.php";
/*
    Here you can register functions for the ULOLE CLI!
    Executing: php ulole run <myFunction> (Here are your arguments (Starting with 3))
*/

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);// */

$CLI = new Custom();


$CLI->register("a", function($args) {
    echo ulole\core\classes\util\secure\AES::encrypt("hallo welt", "mein key");
    return "";
});