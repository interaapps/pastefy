<?php
namespace ulole\core\classes\util\cookies;

use \ulole\core\classes\util\Str;
use \ulole\core\classes\util\File;
use \ulole\core\classes\util\JSON;

class Session {
    private $sessionName, $sessionKeys, $sessionPath, $loadedSessionPath=false;
    public function __construct($name="ulole_session") {
        $this->sessionName = $name;
        $this->createIfNotExists();
        
        if (!$this->loadedSessionPath) 
            $this->sessionPath = "ulole/storage/sessions/".Cookies::get($name).".session";
        
        $this->loadSessionKeys();
    }

    public function createIfNotExists($expire=null) {
        global $_SERVER;
        if ($expire==null)
            $expire = CookieBuilder::WEEK*4;
        if (Cookies::get($this->sessionName) === null) {
            $random = Str::random(100);
            $this->createFileIfNotExists($random);
            (new CookieBuilder($this->sessionName))->value($random)->time($expire)->build();
            return true;
        }
        return false;
    } 

    public function createFileIfNotExists($name) {
        $path = "ulole/storage/sessions/".$name.".session";
        if (!\file_exists($path)) {

            $sessionContents = [
                "agent"=>$_SERVER['HTTP_USER_AGENT'],
                "created"=>\time(),
                "data"=>['_$ulole_LAST_USAGE'=>\time()]
            ];
            $this->loadedSessionPath = true;
            $this->sessionPath = "ulole/storage/sessions/".$name.".session";

            (new Str(json_encode($sessionContents)))->writeFile($path);
        } 
    }
    
    public function loadSessionKeys() {
        $path = $this->sessionPath;
        if (\file_exists($path)) {
            $this->sessionKeys = JSON::fromJson(File::get($path));
        } else {
            $this->sessionKeys = [];
            \ulole\core\classes\Logger::log("ERR: couldn't load the sessionfile ".$path);
        }
    }


    public function deleteSession() {
        unset($this->sessionPath);
        (new CookieBuilder($this->sessionName))->value(null)->time(0)->build();
    }

    public function get($key) {
        return $this->sessionKeys->data->{$key};
    }

    public function isset($key) {
        return isset($this->sessionKeys->data->{$key});
    }

    public function set($key, $value) {
        $this->sessionKeys->data->{$key} = $value;
        return $this;
    }

    public function save() {
        (new Str(json_encode($this->sessionKeys)))->writeFile($this->sessionPath);
    }

}