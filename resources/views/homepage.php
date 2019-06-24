<?php tmpl('header'); ?>
WELCOME
<?php tmpl('footer'); ?>
<?php
loadDB("TestTable");
$x = new TestTable;

$qu = $x->select('*')->get();

foreach ($qu as $xasfdObj) {
    echo $xasfdObj->username;
}

use user_modules\testmodule\Main;
new Main();
?>