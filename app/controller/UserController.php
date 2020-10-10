<?php
namespace app\controller;

use modules\deverm\Response;
use \app\classes\User;

class UserController {

    public static function login() {
        global $ULOLE_CONFIG_ENV;
        if (isset($_GET["userkey"])) {
            if (\app\classes\User::getUserInformation($_GET["userkey"]) !== false) {
                $key = \app\classes\User::getUserInformation($_GET["userkey"])->userkey;
                $newUser = new \app\classes\User($key);
                $newUser->login();
                \setcookie("InteraApps_auth", $newUser->session, time()+1593600, "/");
                
                Response::redirect('/');
            }
        } else {
            Response::redirect($ULOLE_CONFIG_ENV->Auth->returnurl);
        }    
    }

    public static function getUser(){
        $out = [
            "loggedIn" => false
        ];

        if (User::loggedIn()) {
            $out["loggedIn"] = true;
            $information = User::getUserObject();
            $out["id"] = $information->id;
            $out["name"] = $information->username;
            $out["profilePicture"] = $information->profilepic;
            $out["color"] = $information->color;
        }

        return $out;
    }

}
