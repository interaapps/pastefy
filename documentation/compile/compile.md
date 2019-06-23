# Compile
Compile is a Template Engine Builder for view.php and a Javascript and CSS bundler.


## PHP Render engine (view.php)
To use it you have just to add a file into the folder (It does work also recursive) resources/compile/views with the extension view.php!
If you'll execute ```php ulole compile``` it will copy it into the resources/views folder in a "compiled form"!

More information in the view.php.md file!

## Javascript and CSS bundler
If you don't want to use webpack with nodejs you can use compile.
To use it you have to go in to the folder resources/compile/js or ~/css and add a compile.json. In it you can add "compile" "rules".

More information in the JS_and_CSS_bundler.md file!