# Insert
```php
loadDB("TestTable");
$x = new TestTable;
$x->username = "Moin";
$x->password = "xD";
$x->save();
$qu = $x->select('*')->get();
```