<?php
namespace app\controller;

use app\classes\User;
use modules\helper\Str;
use \databases\PasteTable;
use modules\deverm\Response;
use modules\helper\security\AES;
use modules\helper\security\Hash;

class SharingController {

    public static function share(){
        $res = [
            "done" => false
        ];
        if (isset($_POST["paste"]) && isset($_POST["user"]) && User::loggedIn()) {
            
            $paste = (new PasteTable)->select()
                ->where("id", $_POST["paste"])
                ->andwhere("userid", User::getUserObject()->id)->first();
            
            if (isset($paste["id"]) && User::isFriend($_POST["user"])) {
                
                $share = new \databases\SharedPastesTable;
                $share->user_id = User::getUserObject()->id;
                $user = User::getUserInformation(User::findUser([
                    [
                        "name", "=", $_POST["user"]
                    ]
                ])->userkey);
                if ($user !== false && !isset($user->id))
                        return $res;
                $share->target_id = $user->id;
                $share->paste = $paste["id"];
                $res["done"] = $share->save();

                $notification = new \databases\NotificationsTable;
                $notification->user_id = $user->id;
                $notification->message = User::getUserObject()->username." shared a paste to you! Click to open.";
                $notification->url = "/".$paste["id"];
                $notification->save();
            }
        }

        return $res;
    }

    public static function getSharedPastes(){

        $res = [
            "done" => false,
            "pastes" => []
        ];
        if (User::loggedIn()) {
            $offset = "";

            if (isset($_GET["page"]) && is_numeric($_GET["page"])) {
                $page = (int) $_GET["page"];
                $offset = " OFFSET ";
                $offset .= ( 10*($page-1) );
                if ($_GET["page"] <= 0)
                    $offset = "";
            }

            
            $pastes = (new \databases\SharedPastesTable)
                            ->select()
                            ->where("target_id", User::getUserObject()->id)
                            ->order("created DESC");
            $pastes->query .= " LIMIT 15".$offset." ";
            $pastes = $pastes->get();

            $pasteArray = [];

            foreach ($pastes as $sharedPaste) {
                $paste = (new PasteTable)->select()
                            ->where("id", $sharedPaste["paste"])->first();
                if (isset($paste["id"])) {
                    array_push($pasteArray, [
                        "id"=>$paste["id"],
                        "content"=>substr($paste ["password"] == "" ? \modules\helper\security\AES::decrypt($paste["content"], $paste["id"]) : "", 0, 400),
                        "title"=>\modules\helper\security\AES::decrypt($paste["title"], $paste["id"]),
                        "encrypted"=>$paste["encrypted"],
                        "using_password"=>$paste["password"] != "",
                        "created"=>$paste["created"],
                    ]);
                } else {
                    (new \databases\SharedPastesTable)->delete()->where("paste", $sharedPaste["paste"])->run();
                }
            }
            $res["done"] = true;
            $res["pastes"] = $pasteArray;
        }

        return $res;
    }

}