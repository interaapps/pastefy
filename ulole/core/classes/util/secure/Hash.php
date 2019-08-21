<?php
namespace ulole\core\classes\util\secure;

class Hash {

    // SHA

    /**
     * @deprecated
     */
    public static function sha1($value) {
        return hash("sha1", $value);
    }
    
    public static function sha256($value) {
        return hash("sha256", $value);
    }
    
    public static function sha384($value) {
        return hash("sha384", $value);
    }
    
    public static function sha512($value) {
        return hash("sha512", $value);
    }

    // md
    public static function md2($value) {
        return hash("md2", $value);
    }
    public static function md4($value) {
        return hash("md4", $value);
    }
    public static function md5($value) {
        return hash("md5", $value);
    }

    // Ripemd
    public static function ripemd128($value) {
        return hash("sha128", $value);
    }
    public static function ripedmd160($value) {
        return hash("sha160", $value);
    }
    public static function ripedmd256($value) {
        return hash("sha256", $value);
    }
    public static function ripedmd320($value) {
        return hash("sha320", $value);
    }

    // Whirlpool
    public static function whirlpool($value) {
        return hash("whirlpool", $value);
    }

    // Tiger
    public static function tiger128_3($value) {
        return hash("tiger128,3", $value);
    }
    public static function tiger160_3($value) {
        return hash("tiger160,3", $value);
    }
    public static function tiger192_3($value) {
        return hash("tiger192,3", $value);
    }
    public static function tiger128_4($value) {
        return hash("tiger128,4", $value);
    }
    public static function tiger160_4($value) {
        return hash("tiger160,4", $value);
    }
    public static function tiger192_4($value) {
        return hash("tiger192,4", $value);
    }

    // snefru
    public static function snefru($value) {
        return hash("snefru", $value);
    }

    // gost
    public static function gost($value) {
        return hash("gost", $value);
    }

    // adler
    public static function adler32($value) {
        return hash("adler32", $value);
    }

    // crc
    public static function crc32($value) {
        return hash("crc32", $value);
    }
    public static function crc32b($value) {
        return hash("crc32b", $value);
    }

    

    public static function hash($algo, $value) {
        return hash($algo, $value);
    }


}