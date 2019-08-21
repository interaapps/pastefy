#Select
loadDB("TestTable");
```php
$x = new databases\TestTable;

$qu = $x->select('*')->get();

foreach ($qu as $xasfdObj) {
    echo $obj->username;
}
```

Where
```php
$x = new databases\TestTable;

$qu = $x->select('*')->where("username","Jeff")->get();

foreach ($qu as $obj) {
    echo $obj->username;
}
```