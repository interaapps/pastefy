<?php
namespace ulole\core\classes\util;

/*

*/

class HashMap {

    public $map;

    public function __construct() {
        $this->map = [];
    }

    public function put($key, $value) {
        $this->map[$key] = $value;
    }

    public function get($key) {
        return $this->map[$key];
    }

    public function containsValue($value) {
        return in_array($value, $this->map);
    }

    public function containsKey($key) {
        return array_key_exists($key, $this->map);
    }

    public function remove($key) {
        unset($this->map[$key]);
    }

    public function isEmpty() {
        return $map == [];
    }


    public function size() {
        return count($this->map);
    }

    public function keySet() {
        $out = [];
        foreach($this->map as $key=>$value) {
            array_push($out, $key);
        }
        return $out;
    }

    public function values() {
        $out = [];
        foreach($this->map as $key=>$value) {
            array_push($out, $value);
        }
        return $out;
    }

    public function entrySet() {
        return $this->map;
    }

    public function getPHPArray() {
        return $this->map;
    }

}