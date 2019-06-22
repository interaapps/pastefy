<?php
namespace uloleframework\CLI;
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
}
