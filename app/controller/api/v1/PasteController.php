<?php
namespace app\controller\api\v1;

use app\classes\Paste;
use modules\deverm\Response;
use databases\PastefyAPITable;

class PasteController {

    public static function get() {
        global $_ROUTEVAR, $_GET;
        Response::setHeader("access-control-allow-origin", "*");
        $password = null;
        if (isset($_GET["password"])) 
            $password = $_GET["password"];
        
        $pasteContents = Paste::getPaste($_ROUTEVAR[1], $password);

        if ($pasteContents["exists"])
            Response::json($pasteContents);
        else Response::json(["done"=>false]);
    }

    /**
     * Creates a new paste.
     * # API KEY REQUIRED
     * 
     * Required POST-Parameters:
     *    apikey: Your api key
     *    contents: Paste-Contents
     * 
     * Optional:
     *    title:
     *    password:   
     *    folder: (Only folder by api-key creator)  
     * 
     * Learn more in Docs: app/docs/v1/create_paste.md
     * */
    public static function createPaste() : array {
        Response::setHeader("access-control-allow-origin", "*");
        $out = [
            "done"=> false
        ];

        
        if ( isset($_POST["content"]) && isset($_POST["apikey"]) ) {
            $api = (new PastefyAPITable)->select()->where("id", $_POST["apikey"])->first();
            if ($api["id"] !== null && $_POST["content"] != "") {
                $paste = new Paste;
                $paste->setContent($_POST["content"]);

                if (isset($_POST["title"]))
                    $paste->setTitle($_POST["title"]);
                if (isset($_POST["folder"]))
                    $paste->setFolder($_POST["folder"]);
                if (isset($_POST["password"]))
                    $paste->setPassword($_POST["password"]);

                $paste->setUser($api["userid"]);
                $out["url"] = $paste->save();
                $out["done"] = true;
            }
        }

        return $out;
    }

}