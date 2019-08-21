<?php
namespace ulole\core\classes\util;

class Str {

    public $string;
    function __construct($string) {
        $this->string = $string;
    }
    public function replace($stringToReplace, $replaceWith=false) {
        $out = $this->string;

        if (is_array($stringToReplace)) {
            foreach ($stringToReplace as $key=>$value) 
                $out = str_replace($key, $value, $out);
        } else {
            $out = str_replace($stringToReplace, $replaceWith,$this->string);
        }

        return $out;
    }

    public function append($string) {
        $this->string .= $string;
        return $this->string;
    }

    public function appendNewLine($string) {
        $this->string .= "\n".$string;
        return $this->string;
    }

    public function clear() {
        $this->string = "";
    }

    public function getString() {
        return $this->string;
    }

    public function writeFile($path) {
        return \file_put_contents($path, $this->string);
    }

    public static function random($length = 10) {
        $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        $charactersLength = strlen($characters);
        $randomString = '';
        for ($i = 0; $i < $length; $i++) {
            $randomString .= $characters[rand(0, $charactersLength - 1)];
        }
        return $randomString;
    } 
}