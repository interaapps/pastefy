<?php
namespace ulole\core\classes\util;

class Str {

    public $string;
    function __construct($string) {
        $this->string = $string;
    }
    function replace($stringToReplace, $replaceWith=false) {
        $out = $this->string;

        if (is_array($stringToReplace)) {
            foreach ($stringToReplace as $key=>$value) 
                $out = str_replace($key, $value, $out);
        } else {
            $out = str_replace($stringToReplace, $replaceWith,$this->string);
        }

        return $out;
    }

    function append($string) {
        $this->string .= $string;
        return $this->string;
    }

    function appendNewLine($string) {
        $this->string .= "\n".$string;
        return $this->string;
    }

    function clear() {
        $this->string = "";
    }

    function getString() {
        return $this->string;
    }

    function writeFile($path) {
        return \file_put_contents($path, $this->string);
    }
}