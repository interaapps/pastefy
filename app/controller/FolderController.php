<?php
namespace app\controller;

use app\classes\User;
use \databases\PasteTable;
use \ulole\core\classes\util\secure\AES;
use \ulole\core\classes\util\secure\Hash;
use \ulole\core\classes\util\Str;

class FolderController {





    public static function redirect($link) {
		echo "<title>Redirecting to ".$link."</title>";
		header("Location: ".$link);
		echo '<meta http-equiv="refresh" content="0;url='.$link.'">';
		echo "<script>window.location.replace('",$link,"')</script>";
		echo "<a href='".$link."'CLICK HERE</title>";
    }

    public static function folder() {
        if (User::usingIaAuth()) {
            global $_ROUTEVAR;
            $folder = (new \databases\PasteFolderTable)->select("*")
                ->where("parent", $_ROUTEVAR[1])->get();
            $pastes = (new PasteTable)->select("*")->where("folder", $_ROUTEVAR[1])->get();
            
            if (User::loggedIn())
                $user = User::getUserObject();
            
            if ( (new \databases\PasteFolderTable)->select("*")->where("id", $_ROUTEVAR[1])->run()->num_rows > 0 ) 
                \view("folder", [
                    "folder"=>$folder,
                    "pastes"=>$pastes,
                    "id"=>$_ROUTEVAR[1],
                    "myfolder"=>((new \databases\PasteFolderTable)->select("userid")->where("id", $_ROUTEVAR[1])->first()->userid == (User::loggedIn() ? $user->id : "NOPE" ) )
                ]);
            else
                \view("404");
        } else
            \view("404");
    }

    public static function getMyDirectories() {
        return json_encode(\app\classes\Utils::getFolder());
    }

    public static function createFolder() {
        if (User::usingIaAuth() && isset($_POST["name"]) && isset($_POST["parent"])) {
            $user = User::getUserObject();
           if ($_POST["parent"]=="") {
                $newFolder = new \databases\PasteFolderTable;
                $newFolder->id = Str::random(8);
                $newFolder->name = $_POST["name"];
                $newFolder->userid = $user->id;
                $newFolder->save();
            } elseif ((new \databases\PasteFolderTable)->select("id")->where("id",$_POST["parent"])->andwhere("userid", $user->id)->run()->num_rows != 0) {
                $newFolder = new \databases\PasteFolderTable;
                $newFolder->id = Str::random(8);
                $newFolder->name = $_POST["name"];
                $newFolder->userid = $user->id;
                $newFolder->parent = $_POST["parent"];
                $newFolder->created = date("Y-m-d H:i:s");
                $newFolder->save();
            }
            self::redirect("/".(( isset($_POST["camefrom"]) ) ? $_POST["camefrom"] : "" ));
        }

    }


}