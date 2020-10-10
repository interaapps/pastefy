<?php 
namespace app\classes;

use \app\classes\User;

class Utils {

    public static $user;
    public static $loggedIn = null;

    public static function getFolder() {
    	if (self::loggedIn()) {
            $out = [];

            $folder = (new \databases\PasteFolderTable)->select("*")
                            ->where("userid", self::getCurrentUser()->id)->get();

            foreach($folder as $obj) {
                $out[self::getChildFolder($obj["name"], $obj["parent"])] = $obj["id"];
            }

            ksort($out);
            return $out;
        }
        return [];
    }

    public static function getChildFolder($parentName, $parentId) {
        $folder = (new \databases\PasteFolderTable)->select("*")
                    ->where("userid", self::getCurrentUser()->id)
                    ->andwhere("id", $parentId);
        
        if (count($folder->get()) > 0 && $parentId != "") {
            $folderObj = $folder->first();
            return self::getChildFolder($folderObj["name"]."/".$parentName, $folderObj["parent"]);
        }
        return "/".$parentName;
    }

    public static function getCurrentUser(){
        if (self::$user === null)
            self::$user = User::getUserObject();
        return self::$user;
    }

    public static function loggedIn() {
        if (self::$loggedIn === null)
            self::$loggedIn = User::loggedIn();
        return self::$loggedIn;
    }

}
