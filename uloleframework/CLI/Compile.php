<?php
namespace uloleframework\CLI;
include "uloleframework/Core/Classes/Replace.php";

class Compile {
    
    public static function compileDir($config, $indir) {
        $conf = json_decode(file_get_contents($config));
        foreach ($conf as $val => $key) {
            $out = "";
            $forout = "";
            foreach ($key as $file) {
                 $out .= file_get_contents($indir."/".$file);
            }
            file_put_contents("public/".$val, $out);
        }
    }

    public static function compileViews($dir, $enddir) {
        $replaceArray = [
            "{{"=>'<?php echo (',
            "}}"=>') ?>'
        ];

        $files = scandir($dir);
        foreach($files as $file) {
            if ($file != ".." && $file != ".")
                if (is_dir($dir."/".$file))
                    Router::autoload($dir."/".$file, $enddir);
                else
                    \file_put_contents($enddir."/".$file, Replace::replaceAsArray($replaceArray, \file_get_contents($dir."/".$file)));
                }
    } 
}
