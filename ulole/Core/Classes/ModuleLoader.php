<?php
namespace uloleframework\Core\Classes;

class ModuleLoader {
    public $manifest, $name, $dir;
    function __construct($name, $dir) {
        $this->name = $name;
        $this->dir = $dir;
        $this->manifest = \json_decode(\file_get_contents($this->dir.$name."/manifest.json"));
    
    }
    function load() {
        foreach ($this->manifest->load as $load) {
            require_once $this->dir.$this->name."/".$load;
        }
    }
}