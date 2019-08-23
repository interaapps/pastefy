<?php
namespace app\classes;

class User {
    
    public $key, $id, $userdata, $session;
    public const SESSIONSDB = "pastefy_sessions";
    
    public function __construct($key, $id="") {
        $userData = self::getUserInformation($key);
        $this->key = $key;
        $this->id  = $userData->id;
    }
    
    public function login() {
        global $MYSQL_DATABASE_CONNECTION;
        $connection = $MYSQL_DATABASE_CONNECTION->con;
        $this->session = self::generateRandomString(100);
        $connection->query('INSERT INTO `'.self::SESSIONSDB.'` (`session_id`, `userid`, `user_key`) VALUES ("'.$this->session.'","'.$this->id.'", "'.$this->key.'");');
    }
    
    public function getUserData() {
        return getUserInformation($this->key);
    }


    public static function findUser($query) {
        global $config_env;
        $postdata = http_build_query(
            ['key' => $config_env->Auth->key,
                "query"=>$query
            ]);
    
        $opts = ['http' =>[
                'method'  => 'POST',
                'header'  => 'Content-Type: application/x-www-form-urlencoded',
                'content' => $postdata
            ]];
        $context  = stream_context_create($opts);
        $result = file_get_contents('https://accounts.interaapps.de/oauth_api/finduser', false, $context);
        $resultJson = json_decode($result);
    
        if ($resultJson->valid)
            return json_decode($result);
        else return false;
    }

    public static function getUserInformation($user) {
        global $config_env;
        $postdata = http_build_query(
            ['key' => $config_env->Auth->key,
                'userkey' => $user
            ]);
    
        $opts = ['http' =>[
                'method'  => 'POST',
                'header'  => 'Content-Type: application/x-www-form-urlencoded',
                'content' => $postdata
            ]];
        $context  = stream_context_create($opts);
        $result = file_get_contents('https://accounts.interaapps.de/oauth_api/getuserinformation', false, $context);
        $resultJson = json_decode($result);
    
        if ($resultJson->valid)
            return json_decode($result);
        else return false;
    }
    
    public static function loggedIn() {
        global $MYSQL_DATABASE_CONNECTION;
        $connection = $MYSQL_DATABASE_CONNECTION->con;
     if (!isset($_COOKIE["InteraApps_auth"])) return false;
        $r1 = $connection->query('SELECT * FROM `'.self::SESSIONSDB.'` WHERE `session_id` = "'.$connection->real_escape_string($_COOKIE["InteraApps_auth"]).'";');
        return $r1->num_rows > 0;
    }
    
    
    public static function getUserObject() {
        global $MYSQL_DATABASE_CONNECTION;
        $connection = $MYSQL_DATABASE_CONNECTION->con;
        $qu1 = $connection->query('SELECT * FROM `'.self::SESSIONSDB.'` WHERE `session_id` = "'.$connection->real_escape_string($_COOKIE["InteraApps_auth"]).'";');
        $qu1Obj = $qu1->fetch_object();
        return self::getUserInformation($qu1Obj->user_key);
    }


    public static function generateRandomString($length = 10) {
        $characters = '0123456789abcdefghijklmnopqrstuvwxyz';
        $charactersLength = strlen($characters);
        $randomString = '';
        for ($i = 0; $i < $length; $i++) {
          $randomString .= $characters[rand(0, $charactersLength - 1)];
        }
        return $randomString;
      }

      public static function usingIaAuth() {
            global $config_env;
            return $config_env->Auth->usingIaAuth;
      }
    
}
