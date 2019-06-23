<?php


class Router {

  public $route;
  public $views_dir;
  public $templates;
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
      echo $GetOrPost[$k1];
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

  function __construct($views_dir, $template="../templates") {
    $route=[];
    $this->route     =  $route;
    $this->template  =  $template;
    $this->views_dir =  $views_dir;
  }

  function set($array) {
    $this->route = $array;
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
        //if ($url == $request) {
            if($method==='POST' && isset($this->GetOrPost[$request]["post"]))
              Router::load($view ,$views_dir.$this->GetOrPost[$request]["post"]);
            elseif($method==='DELETE' && isset($this->GetOrPost[$request]["delete"]))
              Router::load($view,  $views_dir.$this->GetOrPost[$request]["delete"]);
            elseif($method==='PUT' && isset($this->GetOrPost[$request]["put"]))
              Router::load($view,  $views_dir.$this->GetOrPost[$request]["put"]);
            elseif($method==='CONNECT' && isset($this->GetOrPost[$request]["connect"]))
              Router::load($view,  $views_dir.$this->GetOrPost[$request]["connect"]);
            elseif($method==='TRACE' && isset($this->GetOrPost[$request]["trace"]))
              Router::load($view,  $views_dir.$this->GetOrPost[$request]["trace"]);
            elseif($method==='OPTIONS' && isset($this->GetOrPost[$request]["options"]))
              Router::load($view, $views_dir.$this->GetOrPost[$request]["options"]);
            else
              Router::load($view, $views_dir.((!is_callable($view)) ? $view : ""));
          return 0;
        //}

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
   


    public static function load($view, $require) {
      global $_ROUTEVAR;
      if (is_callable($view))
            $view();
        else
          if (strpos($view, "!") !== false) {
            if (strpos($view, "@") !== false)
              echo call_user_func(Router::get_string_between($view, "!", "@").'::'.Router::get_string_between($view, "@", ""));
            else
              echo call_user_func(Router::get_string_between($view, "!", ""));
          } else {
             require $require;
          }
    }


}

function tmpl($template_name) {
  global $templates_dir;
  include $templates_dir.$template_name.".php";
}
