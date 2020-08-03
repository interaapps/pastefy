#!/usr/bin/env php
<?php
$uppmlock = [];
if (file_exists("uppm.locks.json"))
    $uppmlock = json_decode(file_get_contents("uppm.locks.json"));

$uppmconf = [];
if (file_exists("uppm.json"))
    $uppmconf = json_decode(file_get_contents("uppm.json"));

$serverInfo = @json_decode(@file_get_contents("https://raw.githubusercontent.com/interaapps/uppm-packages/master/uppm.json?".rand(00000, 99999), false, stream_context_create([
    "http" => [
        "method" => "GET",
        "header" => "User-Agent: request"
    ]
])));

define("UPPMINFO", [
    "version"=>"1.1.1",
    "server"=> (isset($serverInfo->list)) ? $serverInfo->list : false
]);

?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * Tools
 * 
 * @author InteraApps
 */

 class Tools {

    public static function getStringBetween($string, $start, $end){
        $string = $string;
        $ini = @strpos($string, $start);
        $ini += strlen($start);
        if ($end=="") {
          return substr($string, $ini, strlen($string));
        }
        $len = strpos($string, $end, $ini) - $ini;
        return substr($string , ($start == "") ? 0 : $ini , $len);
    }

    public static function deleteDir($dirPath, $ignore="NON++++++++++++NON+++++++++++++++NON") {
        if (!is_dir($dirPath))
            return;
        
        $files = scandir($dirPath);
        foreach ($files as $file) 
            if ($file != "." && $file != ".." && $file != $ignore) {
                if (is_dir($dirPath."/".$file)) {
                    self::deleteDir($dirPath."/".$file);
                } else {
                    if (!($dirPath=="./" && $file=="uppm"))
                        unlink($dirPath."/".$file);
                }
            }
            
        rmdir($dirPath);
    }

     public static function statusIndicator($done, $total, $size=30) {

         static $start_time;

         if($done > $total) return;

         if(empty($start_time)) $start_time=time();

         $perc=(double)($done/$total);

         $bar=floor($perc*$size);

         $status_bar="\r┋";
         $status_bar.=str_repeat(Colors::GREEN."▉".Colors::ENDC, $bar);
         if($bar<$size){
             $status_bar.=Colors::YELLOW."░".Colors::ENDC;
             $status_bar.=str_repeat(Colors::YELLOW."░".Colors::ENDC, $size-$bar);
         } else {
             $status_bar .= Colors::GREEN."▉".Colors::ENDC;
         }

         $status_bar.="┋ ".number_format($perc*100, 0)." %  $done/$total";


         echo "$status_bar  ";

         flush();

     }


     function copyDir($src, $dst) {
         $dir = opendir($src);
         @mkdir($dst);

         while( $file = readdir($dir) ) {
             if (( $file != '.' ) && ( $file != '..' )) {
                 if ( is_dir($src . '/' . $file) ) {
                     self::copyDir($src . '/' . $file, $dst . '/' . $file);
                 }
                 else {
                     copy($src . '/' . $file, $dst . '/' . $file);
                 }
             }
         }
         closedir($dir);
     }

     public static function downloadAutoloader(){
         if (file_exists("autoload.php")) {
             Colors::info("The file: 'autoload.php' already exists. Do you want to override it? [YES,No]");
             if (!(readline() == "" || strtoupper(readline()) == "YES"))
                return;

         }

         file_put_contents("autoload.php", file_get_contents("https://raw.githubusercontent.com/interaapps/uppm/master/autoload.php"));
     }

 }

 ?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * INIT
 * 
 * @author InteraApps
 */

 class Init {

    public static function initFromCLI() {
        $prefix = "\nUPPM INIT: ";
        if (file_exists("uppm.json")) {
            echo $prefix."uppm.json already initialized! Do you want to reinitialize it? (yes, y)";

        }
        echo $prefix."Project name (Only lower case, numbers and underscores): ";
        $name = readline();
        if (!preg_match('#^[a-z0-9_]+$#', $name) && $name != "") {
            return Colors::PREFIX_ERROR."Only lower case, numbers and underscores are allowed!\n";
        }

        echo $prefix."Project version (Numbers and dots): ";
        $version = readline();
        if (!preg_match('#^[0-9.]+$#', $version) && $version != "") {
            return Colors::PREFIX_ERROR."Only numbers and dots!\n";
        }

        echo $prefix."Description [OPTIONAL]: ";
        $description = readline();

        echo $prefix."Author [OPTIONAL]: ";
        $author = readline();

        echo $prefix."Keywords (Split with a komma) [OPTIONAL]: ";
        $keywords = explode(',', readline());

        self::initProject($name, $version, $description, $author, $keywords);
        Colors::info("You can install the autoloader by typing: uppm autoload");
    }

    public static function initProject(
        $name="uppmproject",
        $version="1.0",
        $description="",
        $author= "",
        $keywords=[]
    ) {
        $file = Configs::getNPPMFile();
        $file->name=$name;
        $file->version=$version;
        $file->description=$description;
        $file->author   =$author;
        $file->keywords =$keywords;
        file_put_contents("uppm.json", json_encode($file, JSON_PRETTY_PRINT));
    }

 }
 ?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * Colors
 * Using colors in the CLI!
 * 
 * @author InteraApps
 */

 class Configs {
    public static function getLockFile() {
        if (file_exists("uppm.locks.json"))
            return json_decode(file_get_contents("uppm.locks.json"));
        
        file_put_contents("uppm.locks.json",'
        {
            "init_scripts": [
        
            ],
            "cli_scripts": {
        
            },
            "packages": {
                
            },
            "directnamespaces": {
                
            }
        }
        ');
        return json_decode(file_get_contents("uppm.locks.json"));
    }

    public static function getNPPMFile() {
        if (file_exists("uppm.json"))
            return json_decode(file_get_contents("uppm.json"));
        
        file_put_contents("uppm.json",'
        {
            "name": "abc",
            "version": "1.0",
            "description": "",
            "author": "",
            "keywords": [],
            "modules": {
                
            },
            "namespaces": {
                
            }
        }
        ');
        return json_decode(file_get_contents("uppm.json"));
    }
 }
 ?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * Install
 * 
 * @author InteraApps
 */

 class Install {

    private $downloadUrl,
            $type,
            $enddir = false,
            $webContext,
            $name,
            $version;

    public function __construct($name, $version, $output=true) {
        $this->name    = $name;
        $this->version = $version;
        if ($output) if ($output) Tools::statusIndicator(0, 100);
        if ($version == ":github"){
            $this->webContext = stream_context_create([
                "http" => [
                    "method" => "GET",
                    "header" => "User-Agent: request"
                ]
            ]);
            $branch = "master";
            $this->type = "web";
            if (strpos($name, "+") !== false) {
                $branch = ("+".Tools::getStringBetween($name, "+", ""));
                $this->name= str_replace( $branch, "", $name);
            }
            $this->downloadUrl = "https://api.github.com/repos/".$this->name."/zipball/".str_replace("+", "", $branch);
        } elseif($version == ":web") {
            $this->type = "web";
            $this->downloadUrl = $name;
        } elseif ($version == ":composer" || $version == ":packagist") {
            $this->webContext = stream_context_create([
                "http" => [
                    "method" => "GET",
                    "header" => "User-Agent: request"
                ]
            ]);

            $this->enddir = "::PACKAGIST";

            $this->type = "web";
            $package = json_decode(file_get_contents("https://repo.packagist.org/p/".Tools::getStringBetween($name, "", "@").".json", false, $this->webContext));
            $this->downloadUrl = $package->packages->{Tools::getStringBetween($name, "", "@")}->{Tools::getStringBetween($name, "@", "")}->dist->url;
        } elseif(UPPMINFO["server"] !== false) {
            $list = @json_decode(@file_get_contents((UPPMINFO["server"])), true);
            global $uppmconf;
            if (isset($uppmconf) && isset($uppmconf->repositories)) {
                foreach ($uppmconf->repositories as $repository => $link) {
                    $list = array_merge($list, @json_decode(@file_get_contents($link,false, stream_context_create([ "http" => [ "method" => "GET", "header" => "User-Agent: request" ] ])), true));
                }
            }
            $list = json_decode(json_encode($list));
            if ($list->{$this->name}->{$this->version} != null) {
                $this->downloadUrl = $list->{$this->name}->{$this->version};
            }
            $this->type = "normal";
        }
    }

    public function download($output=true) {
        global $uppmconf;
        $enddir = "modules/cbf_".rand(0000,9999); // cbf = Cant be fetched
        if ($output) Tools::statusIndicator(5, 100);
        file_put_contents("UPPMtemp_module.zip", file_get_contents($this->downloadUrl, false, $this->webContext));
        if (class_exists('ZipArchive')) {
            $zip = new ZipArchive;
            if ($output) Tools::statusIndicator(10, 100);
            $res = $zip->open("UPPMtemp_module.zip");
            if ($res === true) {
                if ($output) Tools::statusIndicator(20, 100);
                Tools::deleteDir("UPPMtempdir");
                if ($output) Tools::statusIndicator(25, 100);
                $zip->extractTo("UPPMtempdir");
                if ($output) Tools::statusIndicator(30, 100);
                $zip->close();

                $files = scandir('UPPMtempdir');
                $dirInZip = false;

                $count = (function($files) {
                    $counter = 0;
                    foreach ($files as $f)
                        if ($f != "." && $f != "..")
                            $counter++;
                    return $counter;
                })($files);

                $composerconf = false;

                if (file_exists("UPPMtempdir/uppm.json"))
                    $tempuppmconf = json_decode(file_get_contents("UPPMtempdir/uppm.json"));
                else if (file_exists("UPPMtempdir/composer.json"))
                    $composerconf = json_decode(file_get_contents("UPPMtempdir/composer.json"));

                if ($count == 1) {
                    foreach($files as $file) {
                        if (is_dir("UPPMtempdir/".$file)) {
                            if ($file != "." && $file != "..") {
                                $dirInZip = $file;
                                if (file_exists("UPPMtempdir/".$file."/uppm.json"))
                                    $tempuppmconf = json_decode(file_get_contents("UPPMtempdir/".$file."/uppm.json"));
                                if (file_exists("UPPMtempdir/".$file."/composer.json")) {
                                    $composerconf = json_decode(file_get_contents("UPPMtempdir/" . $file . "/composer.json"));
                                }
                            }
                        }
                    }
                }

                if ($output) Tools::statusIndicator(50, 100);



                if ($this->enddir !== false) {
                    if ($this->enddir == "::PACKAGIST") {
                        if ($composerconf !== false) {
                            $enddir = "modules/".Tools::getStringBetween($composerconf->name, "/", "");
                        }
                    }
                }

                if (isset($tempuppmconf->directory))
                    $enddir = $tempuppmconf->directory;
                elseif (isset($tempuppmconf->name))
                    $enddir = "modules/".$tempuppmconf->name;
                if (is_dir($enddir) && $enddir!="./" )
                    Tools::deleteDir($enddir);
                
                if (!is_dir("modules"))
                    mkdir("modules");

                $copy = false;

                if ($output && (isset($tempuppmconf->directory) ? $tempuppmconf->directory : "") == "./") {
                    echo "\nThis module will be moved to this directory: ".getcwd()." Do you want that? [yes,NO] ";
                    if (strtolower(readline()) != "yes" && strtolower(readline()) != "y")
                        die("Cancelled");
                    $copy = true;
                    $enddir = getcwd()."/";
                }

                if ($dirInZip !== false) {
                    if ($copy)
                        Tools::copyDir("UPPMtempdir/".$dirInZip, $enddir);
                    else
                        rename("UPPMtempdir/".$dirInZip, $enddir);
                } else {
                    if ($copy)
                        Tools::copyDir("UPPMtempdir", $enddir);
                    else
                        rename("UPPMtempdir", $enddir);
                }

                if (isset($tempuppmconf->modules)) {
                    $config = Configs::getNPPMFile();
                    foreach ($tempuppmconf->modules as $name=>$version) {
                        if (!isset($uppmconf->{$name})) {
                            Colors::info("Installing dependency $name $version");
                            (new Install($name, $version))->download();
                            Colors::done("Installed dependency $name");

                            Colors::info("Adding to uppm.json (modules)");
                            $config->modules->{$name} = $version;
                        } else if ($uppmconf->{$name} != $version) {
                            Colors::info("The dependency $name is already installed with the version ".$uppmconf->{$name}." but the dependency $tempuppmconf->name needs the version $version. Do you want to change the dependencies version? yes/NO");
                            if (readline("") == "yes" || readline("") == "y") {
                                (new Install($name, $version))->download();
                                Colors::done("Installed dependency $name");
                                Colors::info("Adding to uppm.json (modules)");
                                $config->modules->{$name} = $version;
                            }
                        } else
                            Colors::info("Dependency $name $version is installed");
                    }
                    file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                }

                if (isset($composerconf->require)) {
                    Colors::info("Checking composer dependencies");
                    $config = Configs::getNPPMFile();
                    foreach ($composerconf->require as $name=>$version) {
                        if (!isset($uppmconf->{$name})) {
                            Colors::info("Checking $name");
                            if (strpos($name, "/") !== false) {

                                Colors::info("Installing dependency ".$name."@".str_replace("^", "v", $version));

                                $package = json_decode(file_get_contents("https://repo.packagist.org/p/".$name.".json", false, $this->webContext));

                                if ( isset($package->{str_replace("^", "v", $version)}->dist->url) )
                                    (new Install($name . "@" . str_replace("^", "v", $version), ":composer"))->download();
                                elseif ( isset($package->{str_replace("^", "", $version).""}->dist->url) )
                                    (new Install($name . "@" . str_replace("^", "", $version)."", ":composer"))->download();
                                elseif ( isset($package->{str_replace("^", "v", $version).".0"}->dist->url) )
                                    (new Install($name . "@" . str_replace("^", "v", $version).".0", ":composer"))->download();
                                else
                                    Colors::error("Couln't find the dependency $name with the version $version");

                                Colors::done("Installed dependency $name");

                                if (strpos($version, "|") !== false)
                                    $version = Tools::getStringBetween($version, "", "|");

                                Colors::info("Adding to uppm.json (modules)");
                                $config->modules->{$name . "@" . str_replace("^", "v", $version)} = ":composer";
                            }
                        } else
                            Colors::info("Dependency $name $version is installed");
                    }
                    file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                }

                if ($output) Tools::statusIndicator(60, 100);

                $lockFile = Configs::getLockFile();
                if (is_array($lockFile->packages) || $lockFile->packages == null) {
                    $lockFile->packages = ["TEMPNULL-------"=>"TEMPNULL-------"];
                }
                $lockFile->packages->{$this->name} = $this->version;
                if (isset($tempuppmconf)) {

                    if (isset($tempuppmconf->directnamespaces)) {
                        if (is_array($tempuppmconf->directnamespaces)) {
                            $tempuppmconf->directnamespaces = ["TEMPNULL-------"=>"TEMPNULL-------"];
                        }

                        foreach ($tempuppmconf->directnamespaces as $key => $val)
                            $lockFile->directnamespaces->{$key} = $val;
                    }
                    if (isset($tempuppmconf->cli_scripts)) {
                        if (is_array($tempuppmconf->cli_scripts)) {
                            $tempuppmconf->cli_scripts = ["TEMPNULL-------"=>"TEMPNULL-------"];
                        }

                        foreach ($tempuppmconf->cli_scripts as $key => $val)
                            $lockFile->cli_scripts->{$key} = $val;
                    }
                }

                rmdir("UPPMtempdir");
                unlink("UPPMtemp_module.zip");

                if ($output) Tools::statusIndicator(80, 100);

                if ($this->enddir !== false && $this->enddir == "::PACKAGIST" && $composerconf !== false && isset($composerconf->autoload->{"psr-4"})) {
                    foreach ($composerconf->autoload->{"psr-4"} as $namespace=>$paths) {
                        foreach ( scandir($enddir."/".$paths) as $file ) {
                            if ($file != "." && $file != ".."){
                                $lockFile->directnamespaces->{$namespace.str_replace(".php","",$file)} = $enddir."/".$paths."/".$file;
                            }
                        }
                    }
                }

                file_put_contents("uppm.locks.json", json_encode($lockFile, JSON_PRETTY_PRINT));
                if ($output) Tools::statusIndicator(100, 100);

                echo "Done\n";
            }
        }
    }

    public static function installNew($name) {
        if (strpos($name, ":") !== false) {
            $type = Tools::getStringBetween($name, "", ":");
            $name = Tools::getStringBetween($name, ":", "");
            $config = Configs::getNPPMFile();
            if ($type=="github") {
                if (is_array($config->modules))
                    $config->modules = [$name=>":github"];
                else
                    $config->modules->{$name} = ":github";
                file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                (new Install($name, ":github"))->download();
            } elseif ($type=="web") {
                if (is_array($config->modules))
                    $config->modules = [$name=>":web"];
                else
                    $config->modules->{$name} = ":web";
                file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                (new Install($name, ":web"))->download();
            } elseif ($type=="composer" || $type=="packagist") {
                if (is_array($config->modules))
                    $config->modules = [$name=>":composer"];
                else
                    $config->modules->{$name} = ":composer";
                file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                (new Install($name, ":composer"))->download();
            }
        } else {
            global $uppmconf;

            $list = @json_decode(@file_get_contents((UPPMINFO["server"])));

            if (isset($uppmconf) && isset($uppmconf->repositories)) {
                foreach ($uppmconf->repositories as $repository => $link)
                    $list = array_merge($list, @json_decode(@file_get_contents($link,false, stream_context_create([ "http" => [ "method" => "GET", "header" => "User-Agent: request" ] ])), true));
            }

            if (strpos($name, "@") !== false) {
                $version = Tools::getStringBetween($name, "@", "");
                $name = Tools::getStringBetween($name, "", "@");
            } elseif (isset($list->{$name}->newest)) {
                $version = $list->{$name}->newest;
            } else {
                echo "Version not found!";
                return "\n";
            }
    
            if (isset($list->{$name}->{$version})) {
                $config = Configs::getNPPMFile();
                if (is_array($config->modules))
                    $config->modules = [$name=>$version];
                else
                    $config->modules->{$name} = $version;
                
                file_put_contents("uppm.json", json_encode($config, JSON_PRETTY_PRINT));
                (new Install($name, $version))->download();
            } else {
                echo "Package not found";
            }
        }
    }

 }

 ?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * INIT
 * 
 * @author InteraApps
 */

class Build {

    private $directory;
    private $outputLocation = "uppm_target";
    private $main = false;
    private $outputFile = "test.phar";
    private $ignoredDirectories = [];
    private $ignoredFiles = [];

    public function __construct(){

    }

    public function build() : void {

        Colors::info("UPPM Build - Building into a phar");

        Colors::info("Building...");

        if (file_exists(getcwd()."/".$this->outputLocation."/".$this->outputFile))
            unlink(getcwd()."/".$this->outputLocation."/".$this->outputFile);
        if (file_exists(getcwd()."/".$this->outputLocation."/".$this->outputFile.".gz"))
            unlink(getcwd()."/".$this->outputLocation."/".$this->outputFile.".gz");

        if (!file_exists($this->outputLocation)) {
            Colors::info("Creating dir: ".$this->outputLocation);
            mkdir($this->outputLocation);
        }

        Colors::info("Initializing PHP-Phar");
        $phar = new Phar(getcwd()."/".$this->outputLocation."/".$this->outputFile);

        Colors::info("BuildFromDirectory: ".$this->directory);
        $phar->buildFromDirectory(getcwd()."/".$this->directory, '/^(?!(.*uppm_target))'.(function(){
            $out = "";
            foreach ($this->ignoredDirectories as $directory)
                $out .= '(?!(.*'.str_replace("/","\\/",$directory).'))';
            return $out;
        })().'(.*)$/i');


        if ($this->main !== false) {
            Colors::info("Setting stub to".$this->main);
            $phar->setDefaultStub($this->main, "/" . $this->main);
        } else
            Colors::warning("main haven't been found in uppm.json (\"build\": {\"main\": NULL (NOT FOUND)})");


        if (file_exists(getcwd()."/".$this->outputLocation."/".$this->outputFile.".gz")) {
            Colors::info("Removing old ".$this->outputFile.".gz");
            unlink(getcwd() . "/" . $this->outputLocation . "/" . $this->outputFile . ".gz");
        }

        Colors::info("Compressing...");
        $phar->compress(Phar::GZ);
        Colors::done("Built into the file ".getcwd()."/".$this->outputLocation."/".$this->outputFile);

        foreach ($this->ignoredFiles as $file) {
            try {
                Colors::info("Removing file in Phar: ".$file);
                $phar->delete($file);
            } catch (BadMethodCallException $e) {}
        }
        Colors::done("Done!");

    }

    public function setDirectory(string $directory) : void{
        $this->directory = $directory;
    }

    public function setOutputFile(string $file) : void{
        $this->outputFile = $file;
    }

    public function setMain(string $main) : void{
        $this->main = $main;
    }

    public function setIgnoredFiles(array $ignoredFiles) : void {
        $this->ignoredFiles = $ignoredFiles;
    }

    public function setIgnoredDirectories(array $ignoredDirectories): void{
        $this->ignoredDirectories = $ignoredDirectories;
    }

    public function setOutputLocation(string $outputLocation): void{
        $this->outputLocation = $outputLocation;
    }

    public function getOutputFile(): string {
        return $this->outputFile;
    }

    public function getOutputLocation(): string {
        return $this->outputLocation;
    }

}

?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 *
 * Tools
 *
 * @author InteraApps
 */

class Archive {

    private $ignore = [];

    public function build($source, $destination) : void {
        if (!file_exists(getcwd()."/uppm_target"))
            mkdir(getcwd()."/uppm_target");
        if (!file_exists(getcwd()."/uppm_target/archives"))
            mkdir(getcwd()."/uppm_target/archives");

        $source = getcwd()."/".$source;

        $destination = getcwd()."/uppm_target/archives/".$destination;
        if (file_exists($destination))
            unlink($destination);

        if (!extension_loaded('zip') || !file_exists($source)) {
            Colors::error("Zip not found");
            return;
        }

        $zip = new ZipArchive();
        if (!$zip->open($destination, ZIPARCHIVE::CREATE)) {
            Colors::error("Error while opening $destination");
            return;
        }

        $source = str_replace('\\', '/', realpath($source));

        $ignore = [];

        foreach ($this->ignore as $ignoreMe) {
            Colors::info("Ignoring ".getcwd()."/".$ignoreMe);
            array_push($ignore, getcwd() . "/" . $ignoreMe);
        }


        if (is_dir($source) === true) {
            $files = new RecursiveIteratorIterator(new RecursiveDirectoryIterator($source), RecursiveIteratorIterator::SELF_FIRST);

            $filesDone = 0;
            $filesCount = 0;
            foreach ($files as $file)
                $filesCount++;

            foreach ($files as $file) {
                $filesDone++;
                Tools::statusIndicator($filesDone, $filesCount, 50);

                $file = str_replace('\\', '/', $file);

                if( in_array(substr($file, strrpos($file, '/')+1), array('.', '..')) )
                    continue;

                $file = realpath($file);

                if (!in_array($file, $ignore)) {
                    if (is_dir($file) === true)
                        $zip->addEmptyDir(str_replace($source . '/', '', $file . '/'));
                    else if (is_file($file) === true)
                        $zip->addFromString(str_replace($source . '/', '', $file), file_get_contents($file));
                }
            }
            echo "\n";
            Colors::done("Done! Zipped to $destination");
        } else if (is_file($source) === true)
            $zip->addFromString(basename($source), file_get_contents($source));

        $zip->close();
    }

    private function ignoreRecursive($dir) {
        foreach (scandir($dir) as $item) {
            if ($item != "." && $item != ".."){
                if (is_dir($dir . "/" . $item)) {
                    array_push($this->ignore, $dir."/".$item);
                    Colors::info("Ignoring directory $dir/$item");
                    $this->ignoreRecursive($dir . "/" . $item);
                } else
                    array_push($this->ignore, $dir."/".$item);
            }
        }
    }

    public function setIgnore(array $ignore): void{
        $this->ignore = $ignore;
        foreach ($ignore as $item)
            if (is_dir($item)) $this->ignoreRecursive($item);
        if (is_dir("uppm_target")) {
            array_push($this->ignore, "uppm_target");
            $this->ignoreRecursive("uppm_target");
        }
    }

}

?>
<?php
class Runner {

    private $file = null;
    private $args = [];
    private $runCommands = [];
    private $compileFirst = false;
    private $uppmLogs = true;
    private $exec = [];


    public function run(){
        global $CLI;
        if ($this->file == null) {
            if (!$this->compileFirst)
                Colors::error("Compile File not set!");
            else {
                if ($this->uppmLogs)
                    Colors::info("Compiling...");
                $build = $CLI->getCommands()["build"]();
                $this->file = "./".$build->getOutputLocation()."/".$build->getOutputFile();
            }
        }

        foreach ($this->exec as $exec)
            system($exec);


        $command = "php ";
        $command .= $this->file;
        foreach ($this->args as $arg)
            $command .= " ".$arg;

        if ($this->uppmLogs)
            Colors::info("Running PHP-Project with command: ".$command);
        system($command);
    }

    public function setFile(string $file): Runner {
        $this->file = $file;
        return $this;
    }

    public function setArgs(array $args): Runner {
        $this->args = $args;
        return $this;
    }

    public function setRunCommands(array $runCommands): Runner {
        $this->runCommands = $runCommands;
        return $this;
    }

    public function setCompileFirst(bool $compileFirst): Runner {
        $this->compileFirst = $compileFirst;
        return $this;
    }

    public function getFile(): string {
        return $this->file;
    }

    public function getArgs(): array {
        return $this->args;
    }

    public function getRunCommands(): array {
        return $this->runCommands;
    }

    public function getCompileFirst(): bool {
        return $this->compileFirst;
    }

    public function isUppmLogs(): bool {
        return $this->uppmLogs;
    }

    public function setUppmLogs(bool $uppmLogs): void {
        $this->uppmLogs = $uppmLogs;
    }

    public function getExec(): array {
        return $this->exec;
    }

    public function setExec(array $exec): void {
        $this->exec = $exec;
    }


}

?>

<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * Colors
 * Using colors in the CLI!
 * 
 * @author InteraApps
 */

 class Colors {
     public const HEADER = "\033[95m",
                  OKBLUE = "\033[94m",
                  OKGREEN = "\033[92m",
                  WARNING = "\033[93m",
                  FAIL = "\033[91m",
                  ENDC = "\033[0m",
                  BOLD = "\033[1m",
                  UNDERLINE = "\033[4m",
                  RED = "\033[31m",
                  BLUE = "\033[34m",
                  YELLOW = "\033[33m",
                  TURQUIOUS = "\033[36m",
                  GREEN = "\033[32m",
                  BLINK = "\033[5m",
                  BG_RED = "\033[41m",
                  BG_BLUE = "\033[44m",
                  BG_GREEN = "\033[42m",
                  BG_YELLOW = "\033[43m",
                  BG_BLACK = "\033[40m";
        
    public const PREFIX_DONE = "\033[92m᮰ Done\033[0m: ",
                 PREFIX_WARN = "\033[93m᮰ WARNING\033[0m: ",
                 PREFIX_INFO = "\033[36m᮰ INFO\033[0m: ",
                 PREFIX_ERROR = "\033[91m᮰ ERROR\033[0m: ";

    public static function info($str){
        echo self::PREFIX_INFO.$str.self::ENDC."\n";
    }

     public static function warning($str){
         echo self::PREFIX_WARN.$str.self::ENDC."\n";
     }

     public static function done($str){
         echo self::PREFIX_DONE.$str.self::ENDC."\n";
     }

     public static function error($str){
         echo self::PREFIX_ERROR.$str.self::ENDC."\n";
     }
 }

 ?>
<?php
/**
 * - ULOLEPHPPACKAGEMANAGER -
 * 
 * CLI
 * 
 * @author InteraApps
 */

 class CLI {
    public $commands = [];
    public $descriptions = [];
    /**
     * Change the not found errormessage
     */
    public $errorMessage;
    /** 
     * Shows a list with all commands on function not found error
     */
    public $showArgsOnError=true;

    
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
    public function run($run) {
        if (isset($this->commands[$run])) {
            $function = ($this->commands[$run]);
            echo $function($run);
        } else {
            if ($this->errorMessage != null) 
                echo $this->errorMessage;
            else
                echo Colors::PREFIX_ERROR."Function \"".$run."\" not found!\n";
            
            
            if ($this->showArgsOnError) {
                $showArgs = Colors::PREFIX_DONE."Those are some valid functions: ";
                foreach ($this->commands as $command=>$value) {
                    $showArgs .= "\n  \033[92m- \033[0m".$command.": ".$this->descriptions[$command];
                }
                echo $showArgs."\n";
            }

        }
    }

     public function getCommands(): array {
         return $this->commands;
     }

     public function getDescriptions(): array {
         return $this->descriptions;
     }
 }

 ?>
<?php
ini_set('phar.readonly',0);
$CLI = new CLI(1);

$CLI->register("", function() {
    global $CLI;

    echo Colors::TURQUIOUS."
            ╔╗─╔╗╔═══╗╔═══╗╔═╗╔═╗
            ║║─║║║╔═╗║║╔═╗║║║╚╝║║
            ║║─║║║╚═╝║║╚═╝║║╔╗╔╗║
            ║║─║║║╔══╝║╔══╝║║║║║║
            ║╚═╝║║║───║║───║║║║║║
            ╚═══╝╚╝───╚╝───╚╝╚╝╚╝
            A PHP PACKAGE MANAGER
                    BY ".Colors::OKBLUE."INTERAAPPS
            ".Colors::ENDC;

    foreach ($CLI->getCommands() as $command=>$execution)
        echo "\n".Colors::TURQUIOUS.$command.Colors::HEADER.": ".Colors::OKBLUE.$CLI->getDescriptions()[$command].Colors::ENDC;

    return Colors::TURQUIOUS."UPPM Version: ".Colors::OKBLUE.UPPMINFO["version"]."\n".Colors::ENDC;
}, "UPPM");

$CLI->register("help", function() {
    global $CLI;
    return $CLI->getCommands()[""]();
});

$CLI->register("-v", function() {
    return Colors::TURQUIOUS."UPPM Version: ".Colors::OKBLUE.UPPMINFO["version"]."\n".Colors::ENDC;
}, "See UPPMs version");

$CLI->register("init", function() {
    return Init::initFromCLI();
}, "Initializing Project");

$CLI->register("init:fast", function() {
    return Init::initProject("uppm project", "1.0", "", "Me", []);
}, "Initializing Project without any information");

$CLI->register("install", function() {
    global $argv, $uppmconf;
    if (count($argv) == 2) {
        Colors::info("Reinstalling packages...");
        $lockFile = Configs::getLockFile();
        $lockFile->packages = ["TEMPNULL-------"=>"TEMPNULL-------"];
        file_put_contents("uppm.locks.json", json_encode($lockFile, JSON_PRETTY_PRINT));
        foreach ($uppmconf->modules as $name=>$version) {
            $resource = new Install($name, $version);
            $resource->download();
        }
    } elseif(count($argv) == 3) {
        return Install::installNew($argv[2]);
    } else
        Colors::error("Argument error! uppm install or uppm install <package>");
}, "uppm install <package> Install a new package
    Types:
      - 'github:' Downloads a project from github (Example: 'user/project' or 'user/project:master')
      - 'web:' Downloads a project from web (Example: https://mywebsite.com/test.zip)
      - 'local:' Unzips a local file 'file.zip'
     uppm install
        Installs every package");

$CLI->register("update", function() {
    global $uppmconf;
    Colors::info("Fetching main repository");
    $list = @json_decode(@file_get_contents((UPPMINFO["server"])));
    if (isset($uppmconf) && isset($uppmconf->repositories)) {
        foreach ($uppmconf->repositories as $repository => $link) {
            Colors::info("Fetching $repository repository from $link");
            $list = array_merge($list, @json_decode(@file_get_contents($link, false, stream_context_create(["http" => ["method" => "GET", "header" => "User-Agent: request"]])), true));
        }
    }

    Colors::info("Updating packages...");
    $updated = 0;
    foreach ($uppmconf->modules as $name=>$version) {
        if ($version == ":github"){
            Colors::info("Fetching from Github isn't available currently...");
        } elseif($version == ":web") {
            Colors::info("Fetching from web isn't available currently...");
        } elseif($version == ":composer" || $version == ":packagist") {
            Colors::info("Fetching from packagist isn't available currently...");
        } else {
            Colors::info("Checking $name...");

            if (isset($list->{$name}->newest) && $list->{$name}->newest != $version) {
                Colors::info("Updating ".$name." from version ".Colors::RED.$version.Colors::ENDC." to ".Colors::GREEN.$list->{$name}->newest.Colors::ENDC."");
                try {
                    Install::installNew($name);
                } catch (Exception $exception) {
                    Colors::error("Error while updating $name");
                }
                $updated++;
            } else
                Colors::info($name." is up to date");
        }
    }
    Colors::done("Updated $updated packages!");
}, "Updating all");

$CLI->register("linuxglobal", function() {
    Colors::info("This action requires root permissions (sudo)");
    exec("
    sudo wget --output-document=tmp__uppm https://raw.githubusercontent.com/interaapps/uppm/master/uppm
    sudo mv tmp__uppm /usr/local/bin/uppm
    sudo chmod 777 /usr/local/bin/uppm
    ");
}, "Installing globally on linux ".Colors::RED."* ".Colors::YELLOW."Root required!");

$CLI->register("serve", function(){
    global $uppmconf;

    $host = "0.0.0.0";
    $port = "8000";
    $directory = ".";
    $routerFile = "";

    if(isset($uppmconf->serve->port)) $port = $uppmconf->serve->port;
    if(isset($uppmconf->serve->host)) $host = $uppmconf->serve->host;
    if(isset($uppmconf->serve->directory)) $directory = $uppmconf->serve->directory;
    if(isset($uppmconf->serve->routerFile)) $routerFile = $uppmconf->serve->routerFile;

    Colors::done("Binding on $port");
    $exec= "cd $directory
    php -S $host:$port -t ./ $routerFile";

    system($exec);
    exec($exec);
    shell_exec($exec);

    Colors::error("Couldn't start the webserver!");

}, "A simple Testserver. It's using the PHPs included one! Please enable exec for it if you didn't.");

$CLI->register("autoload", function(){
    Tools::downloadAutoloader();
});

$CLI->register("run", function() {
    global $uppmconf;
    if ($uppmconf != null) {
        $runner = new Runner();
        if ($uppmconf->run->file != null)
            $runner->setFile($uppmconf->run->file);
        if ($uppmconf->run->arguments != null)
            $runner->setArgs($uppmconf->run->arguments);
        if ($uppmconf->run->exec != null)
            $runner->setExec($uppmconf->run->exec);
        if ($uppmconf->run->compile_first != null)
            $runner->setCompileFirst($uppmconf->run->compile_first);
        $runner->run();
    } else
        Colors::error("Running is not initialized!");

});

$CLI->register("build", function() {
    global $uppmconf;
    $build = new Build();
    $build->setDirectory(".");
    $build->setOutputFile((isset($uppmconf->name)?$uppmconf->name:"project")."-".(isset($uppmconf->version)?$uppmconf->version:"1.0").".phar");

    if(isset($uppmconf->build->main))
        $build->setMain($uppmconf->build->main);
    if(isset($uppmconf->build->output))
        $build->setOutputFile($uppmconf->build->output);
    if(isset($uppmconf->build->src))
        $build->setDirectory($uppmconf->build->src);
    if(isset($uppmconf->build->ignored_directories))
        $build->setIgnoredDirectories($uppmconf->build->ignored_directories);
    if(isset($uppmconf->build->ignored_files))
        $build->setIgnoredFiles($uppmconf->build->ignored_files);

    $build->build();
    return $build;
}, "Build to a .phar file");

$CLI->register("archive", function () {
    global $uppmconf;
    $archive = new Archive();
    $destination = (isset($uppmconf->name)?$uppmconf->name:"project")."-".(isset($uppmconf->version)?$uppmconf->version:"1.0").".zip";
    $source = ".";
    $ignore = [];

    if(isset($uppmconf->archive->ignore))
        $ignore = $uppmconf->archive->ignore;
    if(isset($uppmconf->archive->src))
        $source = $uppmconf->archive->src;
    if(isset($uppmconf->archive->output))
        $destination = $uppmconf->archives->output;

    $archive->setIgnore($ignore);
    $archive->build($source, $destination);
}, "Archives the project!");

$CLI->register("deploy", function () {

}, "Deploy:
        SOON
");

$CLI->register("info", function(){
    global $uppmconf;
    echo Colors::GREEN."---== ".Colors::TURQUIOUS."UPPM".Colors::GREEN." ==---".Colors::ENDC."\n";
    echo "Overview: \n";

    if ($uppmconf->name != null)
        echo Colors::OKBLUE."Name: ".Colors::YELLOW.$uppmconf->name.Colors::ENDC."\n";
    if ($uppmconf->version!= null)
        echo Colors::OKBLUE."Version: ".Colors::YELLOW.$uppmconf->version.Colors::ENDC."\n";
    if ($uppmconf->author!= null)
        echo Colors::OKBLUE."Author: ".Colors::YELLOW.$uppmconf->author.Colors::ENDC."\n";

    echo "\nDependencies: \n";
    foreach ($uppmconf->modules as $module=>$ver)
        echo "- ".Colors::OKBLUE.$module." ".Colors::YELLOW.$ver.Colors::ENDC;
    echo "\n";
    echo Colors::GREEN."---== ".Colors::TURQUIOUS."-==-".Colors::GREEN." ==---".Colors::ENDC."\n";
});

if (isset($argv[1]))
    $CLI->run($argv[1], $argv);
else
    $CLI->run("", $argv);
$lockFile = Configs::getLockFile();
if (isset($lockFile->packages->{"TEMPNULL-------"})) {
    unset($lockFile->packages->{"TEMPNULL-------"});
    file_put_contents("uppm.locks.json", json_encode($lockFile, JSON_PRETTY_PRINT));
}
?>