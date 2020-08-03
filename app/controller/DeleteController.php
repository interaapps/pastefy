<?php
namespace app\controller;

use app\classes\User;
use modules\helper\Str;
use \databases\PasteTable;
use modules\deverm\Response;
use modules\helper\security\AES;
use modules\helper\security\Hash;

class DeleteController {

    public static function deleteFolder($id=null) {
        global $_ROUTEVAR;
        if ($id==null)
            $id = $_ROUTEVAR[1];
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (count((new \databases\PasteFolderTable)->select("id")->where("userid", $user->id)->andwhere("id", $id)->get()) > 0) {

                $deleteFolderR = (new \databases\PasteFolderTable)->select("*")
                    ->where("userid", $user->id)
                    ->andwhere("parent", $id);
                $deletePastesR = (new \databases\PasteTable)->select("*")
                    ->where("userid", $user->id)
                    ->andwhere("folder", $id);
                if (count($deleteFolderR->get()) > 0) 
                    foreach ($deleteFolderR->get() as $obj)
                        self::deleteFolder($obj->id);
                if (count($deletePastesR->get()) > 0) 
                    foreach ($deletePastesR->get() as $obj)
                        self::deletePaste($obj->id, false);

                (new \databases\PasteFolderTable)->delete()->where("userid", $user->id)->andwhere("id", $id)->run();
                Response::redirect("/pasteList");
            } else { echo "a";
                \view("404");}
        } else { echo "b";
            \view("404"); }
    }

    public static function deletePaste($id=null, $redirect=true) {
        global $_ROUTEVAR;
        if ($id==null)
            $id = $_ROUTEVAR[1];
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if (((new \databases\PasteTable)->select("id")->where("userid", $user->id)->andwhere("id", $id)->get()) > 0) {
                (new \databases\PasteTable)->delete()->where("userid", $user->id)->andwhere("id", $id)->run();
                if ($redirect)
                    Response::redirect("/pasteList");
            } else
                \view("404");
        } else 
            \view("404");
    }


}