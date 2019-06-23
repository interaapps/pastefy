<?php
    /*
        view.php - ULOLE-FRAMEWORK

        Some examples: (Find out more in uloleframework/CLI/Compile.php :: compileViews at $replaceArray)

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
        '@endforeach'=>"<?php endwhile; ?>",
        
        '<?#'=>'<?php',
        '#?>'=>'?>'
    
    */
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title><?php echo ( "HELLO WORLD!" ); ?></title>
</head>
<body>
    <?php
        echo "Hello world!";
        $a = ["a","b"];
    ?>

    <?php if(false):?>
        hi
    <?php else: ?>
        hallool
    <?php endif; ?>

    <?php foreach($a as $v):?>
        <?php echo ( $v ); ?>
    <?php endforeach; ?>
</body>
</html>