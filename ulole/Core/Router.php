<?php


class Router {

  public $route;
  public $views_dir;
  public $template;
  public $GetOrPost;

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
      $this->GetOrPost[$k1] = $v1;
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

  function setDirectories($views_dir, $template="../templates") {
    global $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa, $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa2;
    $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa = $template;
    $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa2 = $views_dir;
    $this->template  =  $template;
    $this->views_dir =  $views_dir;
  }

  function set($array) {
    $this->route = array_merge($this->route, $array);
  }

  function route() {
    global $_ROUTEVAR;
    $route     =  $this->route;
    $template  =  $this->template;
    $views_dir =  $this->views_dir;

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
              if($method===strtoupper($meth) && isset($this->GetOrPost[$url][$meth])) {

                Router::load($this->GetOrPost[$url][$meth] ,  $views_dir.((!is_callable($this->GetOrPost[$url][$meth])) ? $this->GetOrPost[$url][$meth] : ""), $this);
                return 0;
              }
            }
            Router::load($view, $views_dir.((!is_callable($view)) ? $view : ""), $this);

            // if($method==='POST' && isset($this->GetOrPost[$request]["post"]))
            //   Router::load($this->GetOrPost[$request]["post"] ,  $views_dir.((!is_callable($this->GetOrPost[$request]["post"])) ? $this->GetOrPost[$request]["post"] : ""), $this);
            // elseif($method==='DELETE' && isset($this->GetOrPost[$request]["delete"]))
            //   Router::load($this->GetOrPost[$request]["delete"],  $views_dir.$this->GetOrPost[$request]["delete"], $this);
            // elseif($method==='PUT' && isset($this->GetOrPost[$request]["put"]))
            //   Router::load($this->GetOrPost[$request]["put"],  $views_dir.$this->GetOrPost[$request]["put"], $this);
            // elseif($method==='CONNECT' && isset($this->GetOrPost[$request]["connect"]))
            //   Router::load($this->GetOrPost[$request]["connect"],  $views_dir.$this->GetOrPost[$request]["connect"], $this);
            // elseif($method==='TRACE' && isset($this->GetOrPost[$request]["trace"]))
            //   Router::load($this->GetOrPost[$request]["trace"],  $views_dir.$this->GetOrPost[$request]["trace"], $this);
            // elseif($method==='OPTIONS' && isset($this->GetOrPost[$request]["options"]))
            //   Router::load($this->GetOrPost[$request]["options"], $views_dir.$this->GetOrPost[$request]["options"], $this);
            // else {
            //   Router::load($view, $views_dir.((!is_callable($view)) ? $view : ""), $this);
            // }
            
          return 0;
        

      }
    
    }
    
    
    if (!array_key_exists($genrequest, $route))
      $error404 = true;
    if($error404) {
      header('HTTP/1.1 404 Not Found');
      include $views_dir.$route["@__404__@"];
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
      if ($require !== $parent->views_dir."@") {
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
            include $parent->views_dir.$parent->route["@__404__@"];
          }
        }
    }


    function post($route, $func) {
      if (!isset($this->GetOrPost[$route])) $this->GetOrPost[$route] = [];
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
        $this->GetOrPost[$route]["post"] = $func;
    }
  
    function get($route, $func) {
      if (!isset($this->GetOrPost[$route])) $this->GetOrPost[$route] = [];
      $this->route[$route] = $func;
      $this->GetOrPost[$route]["get"] = $func;
    }
  
    function delete($route, $func) {
      if (!isset($this->GetOrPost[$route])) $this->GetOrPost[$route] = [];
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->GetOrPost[$route]["delete"] = $func;
    }
  
    function put($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->GetOrPost[$route] = ["put"=>$func];
    }
  
    function trace($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->GetOrPost[$route] = ["trace"=>$func];
    }

    function connect($route, $func) {
      if (!isset($this->route[$route]))
        $this->route[$route] = "@";
      $this->GetOrPost[$route] = ["connect"=>$func];
    }

}

$routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa = "";
$routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa2 = "";
function tmpl($template_name) {
  global $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa;
  include $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa.$template_name.".php";
}

function view($template_name) {
  global $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa2;
  include $routing_templates_dir23174916234weq59512543eqwtt6ireqwdfsa2."/".$template_name.".php";
}
