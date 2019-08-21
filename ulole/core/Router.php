<?php

class Router {


  public static $lastViewsDirectory;
  public static $lastTemplatesDirectory;
  public $route;
  public $viewsDirectory;
  public $templatesDirectory;
  public $requestMethod;

  public static function autoload($dir) {
    $files = scandir($dir);
    foreach($files as $file) {
        if ($file != ".." && $file != ".")
            if (is_dir($dir."/".$file))
                Router::autoload($dir."/".$file);
              else
                include $dir."/".$file;
            }
  }


  function setRequestMethods($arr) {
    foreach ($arr as $k1=>$v1) {
      $this->requestMethod[$k1] = $v1;
    }
  }
  
  function addNested($array, $path="") {
    foreach($array as $v1 => $v2) {
      if (is_array($v2)) {
        $this->addNested($v2, $path.$v1."/");
      } else {
        // echo $path.$v1;
        $this->route["/".$path.$v1] = $v2;
        //array_push($this->route, "/".$path.$v1, $v2);
      }
    }
  }

  function __construct() {
    $route=[];
    $this->route     =  $route;
  }

  function setDirectories($viewsDirectory, $templatesDirectory="../templatesDirectorys") {
    
    self::$lastTemplatesDirectory = $templatesDirectory;
    self::$lastViewsDirectory = $viewsDirectory;
    $this->templatesDirectory  =  $templatesDirectory;
    $this->viewsDirectory =  $viewsDirectory;
  }

  function set($array) {
    $this->route = array_merge($this->route, $array);
  }

  function route() {
    global $_ROUTEVAR;
    $route     =  $this->route;
    $templatesDirectory  =  $this->templatesDirectory;
    $viewsDirectory =  $this->viewsDirectory;

    $error404 = false;
    $request = str_replace("?".Router::get_string_between($_SERVER['REQUEST_URI'], "?", ""), "", $_SERVER['REQUEST_URI']);
    $genrequest = $request;

    $method = $_SERVER['REQUEST_METHOD'];

    foreach($route as $url=>$view) {
  
      if(preg_match_all('#^' . $url . '$#', $request, $matches)) {
        foreach ($matches as $key=>$val)
            $_ROUTEVAR[$key] = $val[0];
            if (strpos($request, "dele"))
              echo json_encode($_ROUTEVAR);
          
            $methods = ["post", "delete", "put", "connect", "trace", "options"];
            foreach($methods as $meth) {
              if($method===strtoupper($meth) && isset($this->requestMethod[$url][$meth])) {

                Router::load($this->requestMethod[$url][$meth] ,  $viewsDirectory.((!is_callable($this->requestMethod[$url][$meth])) ? $this->requestMethod[$url][$meth] : ""), $this);
                return 0;
              }
            }
            Router::load($view, $viewsDirectory.((!is_callable($view)) ? $view : ""), $this);

            // if($method==='POST' && isset($this->requestMethod[$request]["post"]))
            //   Router::load($this->requestMethod[$request]["post"] ,  $viewsDirectory.((!is_callable($this->requestMethod[$request]["post"])) ? $this->requestMethod[$request]["post"] : ""), $this);
            // elseif($method==='DELETE' && isset($this->requestMethod[$request]["delete"]))
            //   Router::load($this->requestMethod[$request]["delete"],  $viewsDirectory.$this->requestMethod[$request]["delete"], $this);
            // elseif($method==='PUT' && isset($this->requestMethod[$request]["put"]))
            //   Router::load($this->requestMethod[$request]["put"],  $viewsDirectory.$this->requestMethod[$request]["put"], $this);
            // elseif($method==='CONNECT' && isset($this->requestMethod[$request]["connect"]))
            //   Router::load($this->requestMethod[$request]["connect"],  $viewsDirectory.$this->requestMethod[$request]["connect"], $this);
            // elseif($method==='TRACE' && isset($this->requestMethod[$request]["trace"]))
            //   Router::load($this->requestMethod[$request]["trace"],  $viewsDirectory.$this->requestMethod[$request]["trace"], $this);
            // elseif($method==='OPTIONS' && isset($this->requestMethod[$request]["options"]))
            //   Router::load($this->requestMethod[$request]["options"], $viewsDirectory.$this->requestMethod[$request]["options"], $this);
            // else {
            //   Router::load($view, $viewsDirectory.((!is_callable($view)) ? $view : ""), $this);
            // }
            
          return 0;
        

      }
    
    }
    
    
    if (!array_key_exists($genrequest, $route))
      $error404 = true;
    if($error404) {
      header('HTTP/1.1 404 Not Found');
      include $viewsDirectory.$route["@__404__@"];
      return 404;
    }
  }


    
    public static function get_string_between($string, $start, $end){
        $string = ' ' . $string;
        $ini = strpos($string, $start);
        if ($ini == 0) return '';
        $ini += strlen($start);
        if ($end=="") {
          return substr($string, $ini, strlen($string));
        }
        $len = strpos($string, $end, $ini) - $ini;
        return substr($string, $ini, $len);
    }
   


    public static function load($view, $require, $parent=false) {
      global $_ROUTEVAR;
      //echo $require."--";
      if ($require !== $parent->viewsDirectory."@") {
        if (is_callable($view))
              echo $view();
          else
            if (strpos($view, "!") !== false) {
              if (strpos($view, "@") !== false)
                echo call_user_func(  "app\controller\\".Router::get_string_between($view, "!", "@").'::'.Router::get_string_between($view, "@", "") );
              else
                echo call_user_func(Router::get_string_between($view, "!", ""));
            } else {
              require $require;
            }
        } else {
          if ($parent !== false) {
            header('HTTP/1.1 404 Not Found');
            include $parent->viewsDirectory.$parent->route["@__404__@"];
          }
        }
    }


    function post($route, $func) {
      if (!isset($this->requestMethod[$route])) $this->requestMethod[$route] = [];
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
        $this->requestMethod[$route]["post"] = $func;
    }
  
    function get($route, $func) {
      if (!isset($this->requestMethod[$route])) $this->requestMethod[$route] = [];
      $this->route[$route] = $func;
      $this->requestMethod[$route]["get"] = $func;
    }
  
    function delete($route, $func) {
      if (!isset($this->requestMethod[$route])) $this->requestMethod[$route] = [];
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->requestMethod[$route]["delete"] = $func;
    }
  
    function put($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->requestMethod[$route] = ["put"=>$func];
    }
  
    function trace($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->requestMethod[$route] = ["trace"=>$func];
    }

    function connect($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->requestMethod[$route] = ["connect"=>$func];
    }

}

function tmpl($templatesDirectory_name, $vars=false) {
  if ($vars !== false) {
    foreach($vars as $key=>$val){
      global ${$key};
      ${$key} = $val;
    }
  }
  include Router::$lastTemplatesDirectory.$templatesDirectory_name.".php";
}

function view($templatesDirectory_name, $vars=false) {
   if ($vars !== false) {
    foreach($vars as $key=>$val){
      global ${$key};
      ${$key} = $val;
    }
  }
  include Router::$lastViewsDirectory."/".$templatesDirectory_name.".php";
}
