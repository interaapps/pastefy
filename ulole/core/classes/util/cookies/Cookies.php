<?php
namespace ulole\core\classes\util\cookies;

class Cookies {

    public function set($key="", $value="", $time=0, $path="", $domain="", $secure=false, $httponly=false) {
        return setcookie($key, $value, $time, $path, $domain, $secure, $httponly);
    } 

    public static function get($key) {
        global $_COOKIE;
        if (isset($_COOKIE[$key]))
            return $_COOKIE[$key];
        return null;
    }
    /**
     * Example: Cookies::build("myKey")->value("myValue").time(CookieBuilder::HOUR*5);
     */
    public static function build($key) {
        return (new CookieBuilder($key));
    }
}