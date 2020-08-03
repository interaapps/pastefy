<?php
namespace app\controller\language;

use app\classes\Parsedown;
use modules\deverm\Response;

class MarkdownLanguageController {

    public static function markdown() {
        $out = [
            "done"=>false
        ];
        
        if (isset($_POST["markdown"])) {
            $parse = (new Parsedown);
            $parse->setSafeMode(true);

            $markdown = $parse->text($_POST["markdown"]);
            $out["done"] = true;
            $out["out"] = $markdown;
        }

        Response::json($out);
    }

    
}