<?php
namespace app\controller;

use app\classes\User;
use app\classes\Paste;
use modules\helper\Str;
use \databases\PasteTable;
use modules\deverm\Response;
use modules\helper\security\AES;
use modules\helper\security\Hash;
use modules\helper\cookies\Cookies;
use modules\helper\cookies\CookieBuilder;

class PasteController {

    public static function openPaste() {
        global $_ROUTEVAR, $_GET;

        $pasteTable = new PasteTable;
        if (count(($pasteTable)->select('*')->where("id",$_ROUTEVAR[1])->get()) > 0) {
            $content = $pasteTable->select('*')->where("id",$_ROUTEVAR[1])->first();
            $password = $content["id"];
            if (Cookies::isset("\$_pastepw_".$_ROUTEVAR[1]))
                $password = $content["id"].Cookies::get("\$_pastepw_".$_ROUTEVAR[1]);
            if (User::loggedIn())
                $user = User::getUserObject();
            \view("paste", [
                "id"=>$content["id"],
                "pastetitle"=>\htmlspecialchars(AES::decrypt($content["title"], $content["id"])),
                "code"=>\htmlspecialchars(AES::decrypt($content["content"], $password)),
                "mypaste"=>((new \databases\PasteTable)->select("userid")->where("id", $_ROUTEVAR[1])->first()["userid"] == (User::loggedIn() ? $user->id : "NOPE" ) ),
                "needspassword"=>($content["password"] != "" && !Cookies::isset("\$_pastepw_".$_ROUTEVAR[1]))
            ]);
        } else
            \view("404");
    }

    public static function password() {
        global $_ROUTEVAR;
        $pasteTable = new PasteTable;
        if (count($pasteTable->select('*')->where("id",$_ROUTEVAR[1])->get()) > 0) {
            $content = $pasteTable->select('*')->where("id",$_ROUTEVAR[1])->first();
            if ($content["password"] == Hash::sha512($_POST["password"])) {
                // Yes... passwords will be saved as cookie. The reason is, that we don't want to save encryption-keys on servers.
                (new CookieBuilder(
                    "\$_pastepw_".$_ROUTEVAR[1], 
                    $_POST["password"]
                ))->time(CookieBuilder::WEEK*4)->path("/")->build();
                Response::redirect("/".$_ROUTEVAR[1]);
            } else \view("error", ["message"=>"Password is invalid!"]);
        } else \view("404");

    }

    public static function rawPaste() {
        global $_ROUTEVAR, $_GET;

        Response::setContentType("text/plain");
        Response::setHeader("access-control-allow-origin", "*");
        $password = null;
        if (isset($_GET["password"])) 
            $password = $_GET["password"];
        
        $pasteContents = Paste::getPaste($_ROUTEVAR[1], $password);

        if ($pasteContents["exists"])
            return $pasteContents["content"];
        else
            return "Not found";
    }

    public static function createPaste() {
        global $_POST;
        if (trim($_POST["content"]) != "") {
            $paste = new Paste;
            $paste->setContent($_POST["content"]);

            if (isset($_POST["title"]))
                $paste->setTitle($_POST["title"]);
            if (isset($_POST["folder"]))
                $paste->setFolder($_POST["folder"]);
            if (isset($_POST["password"]))
                $paste->setPassword($_POST["password"]);
            if (isset($_POST["encryption"]))
                $paste->setEncryption($_POST["encryption"]);

            if ( (\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false ){
                $paste->setUser(User::getUserObject()->id);
            }
            return ["success"=>true, "id"=>$paste->save()];
        } else return ["success"=>false, "error_message"=>"Insert contents!"];
    }

    public static function pasteList() {
        global $ULOLE_CONFIG_ENV;
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (!$user || !$user->valid) {
                return ["done"=>false];
            }

            $offset = "";

            if (isset($_GET["page"]) && is_numeric($_GET["page"])) {
                $page = (int) $_GET["page"];
                $offset = " OFFSET ";
                $offset .= ( 10*($page-1) );
                if ($_GET["page"] <= 0)
                    $offset = "";
            }

            $folder = (new \databases\PasteFolderTable)->select("id, name, created")->where("userid", $user->id)->andwhere("parent", "")->order("created DESC")->get();
            
            $pastes = (new PasteTable)->select("id, content, title, id, created, password, encrypted")->where("userid", $user->id)->andwhere("folder","")->order("created DESC");
            $pastes->query .= " LIMIT 10".$offset." ";
            $pastes = $pastes->get();

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
                "folder"=>$folder,
                "pastes"=>$pasteArray
            ];

        } else 
            return ["done"=>false];
    }

}
