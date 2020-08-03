<?php 
namespace app\classes;

use \app\classes\User;

class Utils {

    public static function getFolder() {
    	if (User::loggedIn()) {
		$out = [];

		$folder = (new \databases\PasteFolderTable)->select("*")
		                ->where("userid", User::getUserObject()->id)->get();

		foreach($folder as $obj) {
		    $out[$obj["id"]] = self::getChildFolder($obj["name"], $obj["id"]);
		}

		return $out;
        }
        return "null";
    }

    public static function getChildFolder($parentName, $parentId) {
        $folder = (new \databases\PasteFolderTable)->select("*")
                    ->where("userid", User::getUserObject()->id)
                    ->andwhere("parent", $parentId);
        
        if (count($folder->get()) > 0 && $parentId != "") {
            $folderObj = $folder->first();
            return self::getChildFolder($parentName."/".$folderObj["name"], $folderObj["id"]);
        }
        return "/".$parentName;
    }

}
