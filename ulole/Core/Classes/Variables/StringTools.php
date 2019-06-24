<?php

namespace uloleframework\Core\Classes\Variables;

class StringTools {

    public $string;
    function __construct($string) {
        $this->string = $string;
    }
    function replace($stringToReplace, $replaceWith) {
        return replace($stringToReplace, $replaceWith,$this->string);
    }
}