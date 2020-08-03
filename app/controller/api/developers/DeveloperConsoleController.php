<?php
namespace app\controller\api\developers;

use app\classes\User;
use app\classes\Paste;
use modules\helper\Str;
use \databases\PasteTable;
use modules\deverm\Response;
use \databases\PastefyAPITable;
use modules\helper\security\AES;
use modules\helper\security\Hash;
use modules\helper\cookies\Cookies;
use modules\helper\cookies\CookieBuilder;

class DeveloperConsoleController {

    public static function page(){
        global $ULOLE_CONFIG_ENV;
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (!$user || !$user->valid) {
                header("Location: ".$ULOLE_CONFIG_ENV->Auth->returnurl);
                die("");
            }

            view("api/developerconsole");

        } else 
            \view("404");
    }

    public static function list() {
        global $ULOLE_CONFIG_ENV;
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (!$user || !$user->valid)
                return "err";
            
            $out = [];

            foreach ((new PastefyAPITable)->select()->where("userid", $user->id)->get() as $obj) {
                array_push($out, $obj);
            }
            return $out;
        }
    }

    public static function createNewKey(){
        global $ULOLE_CONFIG_ENV;
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (!$user || !$user->valid)
                return "err";
            
            $apiKey = new PastefyAPITable;
            $apiKey->userid = $user->id;
            $apiKey->id = Str::random(35);
            $apiKey->save();
        }
    }

    public static function deleteKey(){
        global $ULOLE_CONFIG_ENV;
        if (User::usingIaAuth() && isset($_POST["id"])) {
            $user = User::getUserObject();
            if (!$user || !$user->valid)
                return "err";
            
            (new PastefyAPITable)->delete()
                ->where("id", $_POST["id"])
                ->andwhere("userid", $user->id)->run();
        }
    }

}