# PHP Render engine (view.php)
To use it you have just to add a file into the folder (It does work also recursive) resources/compile/views with the extension view.php!
If you'll execute ```php ulole compile``` it will copy it into the resources/views folder in a "compiled form"!
#### Examples for the functions 
```php
    // Echos the inserted string (It does work with functions and variables)
    {{ "HELLO WORLD!" }}

    // Executing the code like <?php  ? >
    <?#
        echo "Hello world!";
        $a = ["a","b"];
    #?> 

    // If statement
    @if((false))#
        hi
    @else
        hallool
    @endif

    // Foreach statement
    @foreach(($a as $v))#
        {{ $v }}
    @endforeach

```