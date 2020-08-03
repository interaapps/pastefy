<?php
namespace app\controller\docs;

use app\classes\Parsedown;

class DocsV1Controller {
    
    const PAGES = [
        ""=>"app/docs/v1/overview.md",
        "create_paste"=>"app/docs/v1/create_paste.md",
        "embed"=>"app/docs/v1/embed.md",
        "getpaste"=>"app/docs/v1/getpaste.md"
    ];

    const PAGES_LINKS = [
        "Overview"=>"/docs/v1",
        "Embed"=>"/docs/v1/embed",
        "Create paste"=>"/docs/v1/create_paste",
        "Get paste"=>"/docs/v1/getpaste",

        //"Errors"=>"/docs/v1/error"
    ];

    public static function page() {
        global $_ROUTEVAR;

        
        if (isset(self::PAGES[$_ROUTEVAR[1]]) ) {
            $file = (new Parsedown)->text(file_get_contents(self::PAGES[$_ROUTEVAR[1]]));
            return view("docs/v1", [
                "doc"=>$file,
                "pages"=>DocsV1Controller::PAGES_LINKS
            ]);
        }
        
        return view("404");
    }

}