<?php

class Custom {
    public $commands;
    public function register(string $name, $func) {
        $this->commands[$name] = $func;
    }
    public function run($run, $argv) {
        if (isset($this->commands[$argv[2]])) {
            $function = ($this->commands[$argv[2]]);
            echo $function($argv);
        } else {
            echo "\033[91m᮰ ERROR\033[0m: Function \"".$argv[2]."\" not found!\n";
        }
    }
}