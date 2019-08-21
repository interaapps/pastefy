<?php 
namespace ulole\core\classes;

class Logger {
    public static function log($text) {
        $path = "ulole/storage/logs/logfile_".date("d_m_y").".ulolelog";
        \file_put_contents($path, ((\file_exists($path))?\file_get_contents($path)."\n":"")."[".date("H:i")."] ".$text);
    }
}