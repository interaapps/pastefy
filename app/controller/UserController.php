<?php
namespace app\controller;



class UserController {

    public static function login() {
        echo \json_encode(\app\classes\User::getUserInformation($_GET["userkey"]));
        if (isset($_GET["userkey"])) {
            if (\app\classes\User::getUserInformation($_GET["userkey"]) !== false) {
                $key = \app\classes\User::getUserInformation($_GET["userkey"])->userkey;
                $newUser = new \app\classes\User($key);
                $newUser->login();
                \setcookie("InteraApps_auth", $newUser->session, time()+1593600, "/");
                
                self::redirect('/');
            }
        }        
    }


    public static function redirect($link) {
		echo "<title>Redirecting to ".$link."</title>";
		echo '<meta http-equiv="refresh" content="0;url='.$link.'">';
		echo "<script>window.location.replace('",$link,"')</script>";
        echo "<a href='".$link."'CLICK HERE</title>";
        header("Location: ".$link);
        exit();
    }
}
