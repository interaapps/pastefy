<?php
namespace app\controller;

use app\classes\User;
use modules\helper\Str;
use \databases\NotificationsTable;
use modules\deverm\Response;
use modules\helper\security\AES;
use modules\helper\security\Hash;

class NotificationsController {

    public static function getAll(){
        if (User::loggedIn()){
            $query = (new NotificationsTable)
                ->select()
                ->where("user_id", User::getUserObject()->id);

            if (isset($_GET["not_received"])) {
                $query->andwhere("received", "0");
            } 

            if (isset($_GET["not_read"])) {
                $query->andwhere("already_read", "0");
            }

            

            $result = $query->get();
            (new NotificationsTable)
                    ->update()
                    ->set("received", "1")
                    ->where("received", "0")
                    ->andwhere("user_id", User::getUserObject()->id)->run();
            return $result;
        }
        return [];
    }

    public static function read(){
        if (User::loggedIn() && isset($_POST["id"]))
            (new NotificationsTable)->update()
                ->set("already_read", "1")
                ->where("id", $_POST["id"])
                ->andwhere("user_id", User::getUserObject()->id)->run();
    }

}