<?php
require "ulole/CLI/Custom.php";
/*
    Here you can register functions for the ULOLE CLI!
    Executing: php ulole run <myFunction> (Here are your arguments (Starting with 3))
*/

$CLI = new Custom();

$CLI->showArgsOnError = false;

$CLI->register("myFunction", function($args) {
    echo "This is my custom function :D!\n";
    return "You can also return a string to echo\n";
});
