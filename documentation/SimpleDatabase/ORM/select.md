loadDB("TestTable");
```php
$x = new TestTable;

$qu = $x->select('*')->get();

foreach ($qu as $xasfdObj) {
    echo $xasfdObj->username;
}
```