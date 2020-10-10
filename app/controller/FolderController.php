<?php
namespace app\controller;

use app\classes\User;
use modules\helper\Str;
use \databases\PasteTable;
use modules\deverm\Response;
use modules\helper\security\AES;
use modules\helper\security\Hash;

class FolderController {

    public static function folder() {
        if (User::usingIaAuth()) {
            global $_ROUTEVAR;
            
            $folder = (new \databases\PasteFolderTable)->select("id, name, created, parent")->where("id", $_ROUTEVAR[1])->first();
            
            if (User::loggedIn())
                $user = User::getUserObject();

            
            if ( isset($folder["id"]) ) {
                $folders = (new \databases\PasteFolderTable)->select("id, name, created")
                            ->where("parent", $_ROUTEVAR[1])->get();

                $pastes = (new PasteTable)->select("id, content, title, id, created, password, encrypted")->where("folder", $_ROUTEVAR[1])->get();

                $pasteArray = [];

                foreach ($pastes as $paste) {
                    array_push($pasteArray, [
                        "id"=>$paste["id"],
                        "content"=>substr($paste ["password"] == "" ? \modules\helper\security\AES::decrypt($paste["content"], $paste["id"]) : "", 0, 400),
                        "title"=>\modules\helper\security\AES::decrypt($paste["title"], $paste["id"]),
                        "encrypted"=>$paste["encrypted"],
                        "using_password"=>$paste["password"] != "",
                        "created"=>$paste["created"],
                    ]);
                }

                return [
                    "done"=>true,
                    "name"=>$folder["name"],
                    "folder"=>$folders,
                    "pastes"=>$pasteArray,
                    "id"=>$folder["id"],
                    "parent"=>$folder["parent"],
                    "myfolder"=>((new \databases\PasteFolderTable)->select("userid")->where("id", $_ROUTEVAR[1])->first()["userid"] == (User::loggedIn() ? $user->id : "NOPE" ) )
                ];
            } else
                return ["done"=>false];
        } else
            return ["done"=>false];
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
                $newFolder->created = date("Y-m-d H:i:s");
                $newFolder->save();
            } elseif (count((new \databases\PasteFolderTable)->select("id")->where("id",$_POST["parent"])->andwhere("userid", $user->id)->get()) != 0) {
                $newFolder = new \databases\PasteFolderTable;
                $newFolder->id = Str::random(8);
                $newFolder->name = $_POST["name"];
                $newFolder->userid = $user->id;
                $newFolder->parent = $_POST["parent"];
                $newFolder->created = date("Y-m-d H:i:s");
                $newFolder->save();
            }
            Response::redirect("/".(( isset($_POST["camefrom"]) ) ? $_POST["camefrom"] : "" ));
        }

    }


}