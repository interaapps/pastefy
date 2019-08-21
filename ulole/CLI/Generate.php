<?php
require "ulole/CLI/Custom.php";

use ulole\core\classes\util\File;
use ulole\core\classes\util\Str;
/*
    Here you can register functions for the ULOLE CLI!
    Executing: php ulole run <myFunction> (Here are your arguments (Starting with 3))
*/

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);// */

$CLI = new Custom();
$CLI->showArgsOnError = true;

$CLI->register("migration", function($args) {
    
    if (count($args) == 4) {
        $fileName = $args[3];
        $fileName[0] = strtoupper($args[3][0]);
        $fileName = $fileName."Table";
        $replaceWith = ["%classname%"=>$fileName, "%tablename%"=>$args[3]];
        File::write("databases/migrate/".$fileName.".php", 
        (new Str(File::get("ulole/CLI/GeneratorTemplates/Migration.tphp")))->replace($replaceWith)  );
        return "\033[92m᮰ Done\033[0m: Done!\n";
    } else 
        return "\033[91m᮰ ERROR\033[0m: php cli generate migration <TableName>\n";
}, "Generates a migration class in the database/migrate Folder");

$CLI->register("database", function($args) {
    
    if (count($args) == 4) {
        $fileName = $args[3];
        $fileName[0] = strtoupper($args[3][0]);
        $fileName = $fileName."Table";
        $replaceWith = ["%classname%"=>$fileName, "%tablename%"=>$args[3]];
        File::write("databases/".$fileName.".php", 
        (new Str(File::get("ulole/CLI/GeneratorTemplates/DatabaseTable.tphp")))->replace($replaceWith)  );
        return "\033[92m᮰ Done\033[0m: Done!\n";
    } else 
        return "\033[91m᮰ ERROR\033[0m: php cli generate database <TableName>\n";
}, "Generates a database class in the database Folder");
