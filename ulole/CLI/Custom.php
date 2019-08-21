<?php

spl_autoload_register(function($class) {
    @include_once "./".str_replace("\\","/",$class).".php";
});

class Custom {
    public $commands;
    public $descriptions;
    /**
     * Change the not found errormessage
     */
    public $errorMessage;
    /** 
     * Shows a list with all commands on function not found error
     */
    public $showArgsOnError=false;

    /**
     * Register a new command
     * @param String function-name (Command)
     * @param Function function (example:function() {return "Hello world";})
     * @param String (Optional) Description
     */
    public function register(string $name, $func, string $description="") {
        $this->commands[$name] = $func;
        $this->descriptions[$name] = $description;
    }

    /**
     * Runs a command
     */
    public function run($run, $argv) {
        if (isset($this->commands[$argv[2]])) {
            $function = ($this->commands[$argv[2]]);
            echo $function($argv);
        } else {
            if ($this->errorMessage != null) 
                echo $this->errorMessage;
            else
                echo "\033[91m᮰ ERROR\033[0m: Function \"".$argv[2]."\" not found!\n";
            
            
            if ($this->showArgsOnError) {
                $showArgs = "\033[91m᮰\033[0m Those are some valid functions: ";
                foreach ($this->commands as $command=>$value) {
                    $showArgs .= "\n  \033[92m- \033[0m".$command.": ".$this->descriptions[$command];
                }
                echo $showArgs."\n";
            }

        }
    }
}