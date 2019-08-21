<?php
namespace ulole\core\classes\util;

class File {

    /**
     * Returns contents of a file
     * @param String $path path (File)
     * 
     * @return String
     */
    public static function get($path) {
        return \file_get_contents($path);
    }
    /**
     * Writes text into a file
     * @param String $path file
     * @param String $input contents
     */
    public static function write($path, $input) {
        \file_put_contents($path, $input);
    }

}