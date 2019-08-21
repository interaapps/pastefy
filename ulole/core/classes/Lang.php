<?php
namespace ulole\core\classes;

class Lang {

    public static $lang, $langPack;

    public static function setLang($lang) {
        self::$lang = $lang;
        if (file_exists("resources/languages/".$lang.".json"))
            self::$langPack = \json_decode(\file_get_contents("resources/languages/".$lang.".json"));
        else
            \ulole\core\classes\Logger::log("WARN: Couldn't load the languagefile: resources/languages/".$lang.".json");
    }

    public static function getLang() {
        return self::$lang;
    }

    public static function lang($name, $defaultValue=null) {
        if ( isset(self::$langPack->{$name}) )
            $out = self::$langPack->{$name};
        else
            $out = $defaultValue;
        return $out;
    }
}