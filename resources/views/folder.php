<?php tmpl("header", ["title"=>"My Pastes"]); ?>
<script>hljs.initHighlightingOnLoad();</script>
<div id="web_contents">

    <a class="flatbtn" onclick="$('#newfolderin').show();">New Folder</a>
        <a class="flatbtn" href="/#<?php echo ($id); ?>">New Paste</a>
        <div id="newfolderin">
            <form action="/create:folder" method="POST">
                <input type="hidden" name="parent" value="<?php echo ($id); ?>">
                <input type="hidden" name="camefrom" value="folder/<?php echo ($id); ?>">
                <a>Create new folder</a>
                <input class="titleinput" type="text" name="name" placeholder="Folder">
                <input class="submitbutton" type="submit" name="sub" value="Send">
            </form>
        </div>
    <script>$('#newfolderin').hide();</script><br><br>

    <h2>Folder</h2>
    <?php foreach($folder as $obj):?>
        <a class="pasteList_paste_href" href="/folder/<?php echo ( $obj["id"] ); ?>"><div class="pasteList_paste_main">    
            <p class="pasteList_paste_title"><?php echo ( $obj["name"] ); ?></p>
            <p class="pasteList_paste_date"><?php echo ( htmlspecialchars($obj["created"]) ); ?></p>
        </div>
        </a>
    <?php endforeach; ?>

    <h2>Pastes</h2>

    <?php foreach($pastes as $obj):?>
    <?php
    if ($obj["encrypted"] == 1) {
        $title = htmlspecialchars(\modules\helper\security\AES::decrypt($obj["title"], $obj["id"]));
        $content = htmlspecialchars(($obj["password"] == "") ? \modules\helper\security\AES::decrypt($obj["content"], $obj["id"]) : "This paste cannot be previewed. This paste is encrypted with a password.");
    } else {
        $title = htmlspecialchars($obj["title"]);
        $content = htmlspecialchars($obj["content"]);
    }
    ?>
        <a class="pasteList_paste_href" href="/<?php echo ( $obj["id"] ); ?>">
            <div class="pasteList_paste_main">        
                <p class="pasteList_paste_title"><?php echo ( $title ); ?></p>
                <p class="pasteList_paste_content"><pre><code style="font-size: 10px; max-height: 370px; overflow-y: hidden;/*overflow-x: hidden;*/ "><?php echo ( substr($content, 0, 1000) ); ?></code></pre></p>
                <p class="pasteList_paste_date"><?php echo ( htmlspecialchars($obj["created"]) ); ?></p>
            </div>
        </a>
    <?php endforeach; ?>
    <?php if($myfolder):?>
        <a href="/delete:folder/<?php echo ($id); ?>">Delete this folder</a>
    <?php endif; ?>
</div>


<?php tmpl("footer"); ?>