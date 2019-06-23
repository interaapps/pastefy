<?php
namespace uloleframework\CLI;
include "uloleframework/Core/Classes/Replace.php";

class Compile {
    
    public static function compileDir($config, $indir) {
        $conf = json_decode(file_get_contents($config));
        foreach ($conf as $val => $key) {
            $out = "";
            $forout = "";
            foreach ($key as $file) {
                 $out .= file_get_contents($indir."/".$file);
            }
            file_put_contents("public/".$val, $out);
        }
    }

    public static function compileViews($dir, $enddir) {
        $replaceArray = [
            "{{"=>'<?php echo (',
            "}}"=>'); ?>',
            
            // STARTS
            "@if(("=>'<?php if(',
            "@foreach(("=>'<?php foreach(',
            "@while(("=>'<?php while(',
            
            "))#"=>"):?>",
            '@else'=>"<?php else: ?>",
            
            // ENDS
            '@endif'=>"<?php endif; ?>",
            '@endforeach'=>"<?php endforeach; ?>",
            '@endwhile'=>"<?php endwhile; ?>",
            
            '<?#'=>'<?php',
            '#?>'=>'?>'
        ];

        $files = scandir($dir);
        foreach($files as $file) {
            if ($file != ".." && $file != ".") {
                if (strpos($file, "view.php")) {
                    if (is_dir($dir."/".$file))
                        Router::autoload($dir."/".$file, $enddir);
                    else
                        \file_put_contents($enddir."/".str_replace(".view.php", ".php", $file), \uloleframework\Core\Classes\Replace::replaceByArray($replaceArray, \file_get_contents($dir."/".$file)));
                        echo "\nview.php renderer: rendered: ".$enddir.$dir."/".$file." into ".$enddir."/".str_replace(".view.php", ".php", $file);
                }
            }        
        }
    } 
}
