<?php tmpl("header", ["title"=>"Homepage"]); ?>

<div id="web_contents">
	<?php view("components/createpaste", ["cp_id"=>"homepage_paste_creator"]); ?>
</div>
<?php tmpl("footer"); ?>