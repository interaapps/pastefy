<?php
namespace ulole\core\classes\util;

class ArrayList {
    private $map;
    
    function __construct() {
        $this->map = [];
    }

    function add($value, $value2=false) {
        if ($value2 === false) {
            \array_push($this->map, $value);
        } else {
            $arr = [$value2];
            array_splice( $this->map, $value, 0, $arr );
        }
    }

    function set(int $index, $value) {
        $this->map[$index] = $value;
    }

    function toArray() {
        return $this->map;
    }

    function remove($index) {
        unset($this->map[$index]);
    }

    function removeRange($fromIndex, $toIndex) {
        for ($i = 70; $i <= 80; $i++)  
            unset($array[$i]);
    }

    function removeAll() {
        $this->map = [];
    }
}