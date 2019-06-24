loadDB("TestTable");
```php
$x = new databases\TestTable;

$qu = $x->select('*')->get();

foreach ($qu as $xasfdObj) {
    echo $xasfdObj->username;
}
```