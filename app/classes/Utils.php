<?php 
namespace app\classes;

use \app\classes\User;

class Utils {

    public static function getFolder() {
        $out = [];

        $folder = (new \databases\PasteFolderTable)->select("*")->where("userid", User::getUserObject()->id)->get();

        foreach($folder as $obj) {
            $out[$obj->id] = self::getChildFolder($obj->name, $obj->id);
        }

        return $out;
    }

    public static function getChildFolder($parentName, $parentId) {

        $folder = (new \databases\PasteFolderTable)->select("*")
                    ->where("userid", User::getUserObject()->id)
                    ->andwhere("parent", $parentId);
        if ($folder->run()->num_rows > 0 && $parentId != "") {
            $folderObj = $folder->first();
            return self::getChildFolder($parentName."/".$folderObj->name, $folderObj->id);
        }
        return "/".$parentName;
    }

}