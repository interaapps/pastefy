<?php
namespace ulole\core\classes\util\cookies;

class CookieBuilder {
    public const SECOND=1,
                 MINUTE=60, 
                 HOUR=3600,
                 DAY=86400,
                 WEEK=604800,
                 YEAR=31536000;

    public $key,
           $value,
           $time,
           $path,
           $domain,
           $secure,
           $httponly;

    public function __construct($key="", $value="", $time=0, $path="", $domain="", $secure=false, $httponly=false) {
        $this->key       =  $key;
        $this->value     =  $value;
        $this->time      =  $time;
        $this->path      =  $path;
        $this->domain    =  $domain;
        $this->secure    =  $secure;
        $this->httponly  =  $httponly;
    }

    public function key($key) {
        $this->key = $key;
        return $this;
    }
    
    public function value($value) {
        $this->value = $value;
        return $this;
    }
    /**
     * Adds to the time() function the given $time parameters
     * 
     * @param Integer $time
     * @return CookieBuilder
     */
    public function time(int $time=0) {
        $this->time = \time()+$time;
        return $this;
    }

    public function hardTime($time) {
        $this->time = $time;
        return $this;
    }

    public function path($path) {
        $this->path = $path;
        return $this;
    }

    public function domain($domain) {
        $this->domain = $domain;
        return $this;
    }

    public function secure() {
        $this->secure = true;
        return $this;
    }

    public function httponly() {
        $this->httponly = true;
        return $this;
    }

    /**
     * Saves the cookie
     */
    public function build() {
        return setcookie($this->key, $this->value, $this->time, $this->path, $this->domain, $this->secure, $this->httponly);
    }
    /**
     * Does the same like build()
     */
    public function save() {
        return $this->build;
    }

}
