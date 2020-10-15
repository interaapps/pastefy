<?php
namespace app\classes;

use app\classes\User;
use modules\helper\Str;
use \databases\PasteTable;
use modules\helper\security\AES;
use modules\helper\security\Hash;
use modules\helper\cookies\Cookies;
use modules\helper\cookies\CookieBuilder;

class Paste {

    private $folder;
    private $title;
    private $content;
    private $password = "";
    private $user = "0";
    private $encryption = 1;

    public function save() : string {
        $id = (function() {
            $id = Str::random(8);
            while( count((new PasteTable)->select('id')->where("id",$id)->get()) > 0) {
                $id = Str::random(8);
            }
            return $id;
        })();
        $pasteTable = new PasteTable;
        $pasteTable->id = $id;

        if (isset($this->folder) && ((\app\classes\User::usingIaAuth()) ? \app\classes\User::loggedIn() : false)) {
            $folder = (new \databases\PasteFolderTable)->select("id")
                    ->where("userid", \app\classes\User::getUserObject()->id)
                    ->andwhere("id", $this->folder);
            if (count($folder->get()) > 0)
                $pasteTable->folder = $folder->first()["id"];
        }

        $pasteTable->title = $this->encryption === 1 ? AES::encrypt($this->title, $id) : $this->title;


        if ($this->encryption === 1 && isset($this->password) && $this->password != "")
            $pasteTable->password = Hash::sha512($this->password);

        $pasteTable->content = $this->encryption === 1 ? AES::encrypt($this->content, $id.((isset($this->password)) ? $this->password : "")) : $this->content;
        $pasteTable->created = date("Y-m-d H:i:s");
        $pasteTable->encrypted = $this->encryption;
        if ($this->user !== null){
            $pasteTable->userid = $this->user;
        }
        $pasteTable->save();
        return $id;
    }

    
    public static function getPaste($paste, $password=null) {
        $pasteTable = new PasteTable;
        $content = $pasteTable->select('id, content, title, created, password, userid, encrypted')->where("id",$paste)->first();
    
        if ($content != null) {
            if ($password!=null)
                $password = $content["id"].$password;
            else
                $password = $content["id"];
        
            return [
                "exists"=>true,
                "id"=>$content["id"],
                "title"=>$content["encrypted"] == 1 ? AES::decrypt($content["title"], $content["id"]) : $content["title"],
                "created"=>$content["created"],
                "encryption"=>$content["encrypted"],
                "content"=>$content["encrypted"] == 1 ? AES::decrypt($content["content"], $password) : $content["content"],
                "using_password"=>$content["password"] !== null && $content["password"] !== "",
                "userid"=>$content["userid"]
            ];
        } 
        return ["exists"=>false];
    }


    public function getFolder() {
        return $this->folder;
    }

    public function setFolder($folder) {
        $this->folder = $folder;
        return $this;
    }

    public function getTitle(){
        return $this->title;
    }

    public function setTitle($title){
        $this->title = $title;
        return $this;
    }

    public function getContent(){
        return $this->content;
    }

    public function setContent($content){
        $this->content = $content;
        return $this;
    }

    public function getPassword() {
        return $this->password;
    }

    public function setPassword($password) {
        $this->password = $password;

        return $this;
    }

    public function getUser(){
        return $this->user;
    }

    public function setUser($user){
        $this->user = $user;

        return $this;
    }

    public function setEncryption($encryption = 1){
        if (\is_numeric($encryption) && ($encryption == 0 || $encryption == 1 || $encryption == 2))
            $this->encryption = $encryption;
        return $this;
    }

    public function getEncryption(){
        return $this->encryption;
    }
}
