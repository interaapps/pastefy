<?php
namespace app\controller;

use modules\deverm\Response;

class UserController {

    public static function login() {
        if (isset($_GET["userkey"])) {
            if (\app\classes\User::getUserInformation($_GET["userkey"]) !== false) {
                $key = \app\classes\User::getUserInformation($_GET["userkey"])->userkey;
                $newUser = new \app\classes\User($key);
                $newUser->login();
                \setcookie("InteraApps_auth", $newUser->session, time()+1593600, "/");
                
                Response::redirect('/');
            }
        }        
    }

}
