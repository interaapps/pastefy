<?php
namespace app\controller\api\v1;

use app\classes\Paste;
use modules\deverm\Response;

class EmbedController {

    public static function embed(){
        global $_ROUTEVAR, $_GET;

        $password = null;
        if (isset($_GET["password"])) 
            $password = $_GET["password"];
        
        $pasteContents = Paste::getPaste($_ROUTEVAR[1], $password);

        if ($pasteContents["exists"])
            view("api/embed", ["paste"=>$pasteContents]);
        else echo "Not found";
    }

}
