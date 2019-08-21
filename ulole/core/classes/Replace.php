<?php
namespace ulole\Core\Classes;

class Replace {
    
    public static function replaceByArray( array $array, string $string ){
        foreach ($array as $key=>$val)
            $string = str_replace($key, $val,$string);

        return $string;
    }

}