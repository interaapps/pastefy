<?php
namespace app\controller;

use app\classes\User;
use \databases\PasteTable;
use \ulole\core\classes\util\secure\AES;
use \ulole\core\classes\util\secure\Hash;
use \ulole\core\classes\util\Str;

class DeleteController {

    public static function deleteFolder($id=null) {
        global $_ROUTEVAR;
        if ($id==null)
            $id = $_ROUTEVAR[1];
        if (User::usingIaAuth()) {
            $user = User::getUserObject();
            if ((new \databases\PasteFolderTable)->select("id")->where("userid", $user->id)->andwhere("id", $id)->run()->num_rows > 0) {

                $deleteFolderR = (new \databases\PasteFolderTable)->select("*")
                    ->where("userid", $user->id)
                    ->andwhere("parent", $id);
                $deletePastesR = (new \databases\PasteTable)->select("*")
                    ->where("userid", $user->id)
                    ->andwhere("folder", $id);
                if ($deleteFolderR->run()->num_rows > 0) 
                    foreach ($deleteFolderR->get() as $obj)
                        self::deleteFolder($obj->id);
                if ($deletePastesR->run()->num_rows > 0) 
                    foreach ($deletePastesR->get() as $obj)
                        self::deletePaste($obj->id, false);

                (new \databases\PasteFolderTable)->delete()->where("userid", $user->id)->andwhere("id", $id)->run();
                self::redirect("/pasteList");
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
            if ((new \databases\PasteTable)->select("id")->where("userid", $user->id)->andwhere("id", $id)->run()->num_rows > 0) {
                (new \databases\PasteTable)->delete()->where("userid", $user->id)->andwhere("id", $id)->run();
                if ($redirect)
                    self::redirect("/pasteList");
            } else
                \view("404");
        } else 
            \view("404");
    }

    public static function redirect($link) {
		echo "<title>Redirecting to ".$link."</title>";
		header("Location: ".$link);
		echo '<meta http-equiv="refresh" content="0;url='.$link.'">';
		echo "<script>window.location.replace('",$link,"')</script>";
		echo "<a href='".$link."'CLICK HERE</title>";
    }

}