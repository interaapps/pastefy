<?php
namespace ulole\core\classes\util\secure;

class AES {

    private $key,
            $data,
            $method,
            $options = 0;

    function __construct($data = null, $key = null, $blockSize = null, $mode = 'CBC') {
        $this->data = $data;
        $this->key = $key;
        if($blockSize==192 && in_array("", ['CBC-HMAC-SHA1','CBC-HMAC-SHA256','XTS'])){
            $this->method = null;
            \ulole\core\classes\Logger::log("ERR: Invalid blocksize!");
            throw new Exception('Invalid blocksize!');
        }
        $this->method = 'AES-' . $blockSize . '-' . $mode;
    }

    private function getInitializationVector() {
        return '1234567890123456';
    }
    
    public function decryptThis() {
        if ($this->data != null && $this->method != null)
            return trim(openssl_decrypt($this->data,
                $this->method,
                $this->key,
                $this->options,
                $this->getInitializationVector()
            ));
        else {
            \ulole\core\classes\Logger::log("ERR: Given Invalid parameters!");
            throw new Exception("Given Invalid parameters!");
        }
    }

    public function encryptThis() {
        if ($this->data != null && $this->method != null) 
            return trim(openssl_encrypt($this->data,
                $this->method,
                $this->key,
                $this->options,
                $this->getInitializationVector()
            ));
        else {
            \ulole\core\classes\Logger::log("ERR: Given Invalid parameters!");
            throw new Exception('Given Invalid parameters!');
        }
    }

    public static function encrypt($inputText, $inputKey, $blockSize = 256) {
        if ($inputText != "") {
            $aes = new AES($inputText, $inputKey, $blockSize);
            return $aes->encryptThis();
        } else return "";
    }

    public static function decrypt($inputText, $inputKey, $blockSize = 256) {
        if ($inputText == "") return "";
        $aes = new AES($inputText, $inputKey, $blockSize);
        return $aes->decryptThis($inputText);
    }
}