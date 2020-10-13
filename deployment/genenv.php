<?php
if(file_exists("env.json")){
    exit();
}
echo "IMPORTING .env FILE";
$env = null;
if (file_exists(".env")){
    $lines = explode("\n", file_get_contents(".env"));
    $env = [];
    foreach($lines as $line){
        $splitted = explode("=", $line);
        $env[$splitted[0]] = $splitted[1];
    }
}

function env($key){
    global $env;
    if ($env !== null)
        return $env[$key];
    return getenv($key);
}

file_put_contents("env.json", json_encode([
    "databases"=> [
        "main"=> [
            "driver"=> "mysql",
            "username"=> env("MYSQL_USERNAME"),
            "password"=> env("MYSQL_PASSWORD"),
            "database"=> env("MYSQL_DATABASE"),
            "server"=> env("MYSQL_SERVER"),
            "port"=> 3306
        ]
    ],
    "Auth"=> [
        "usingIaAuth"=>true,
        "returnurl"=> env("IAAUTH_URL"),
        "key"=> env("IAAUTH_KEY")
    ]
]));

echo "\n";
