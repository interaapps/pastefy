<?php tmpl("header", ["title"=>"Paste"]); ?>
<script>hljs.initHighlightingOnLoad();</script>
<div class="content">
    <?php if($needspassword):?>
        <div id="web_contents">
            <form action="/p/login/<?php echo ($id); ?>" method="POST">
                <a>This Paste is protected with a password!</a>
                <input class="titleinput" type="text" name="password" placeholder="Folder">
                <input class="submitbutton" type="submit" name="sub" value="Send">
            </form>
        </div>
    <?php else: ?>
        <div id="pastecode">
            <h2 style="font-weight: normal"><?php echo ($pastetitle); ?></h2>
            <div id="copyRaw"><pre><code><?php echo ($code); ?></code></pre></div>
            <div id="pasteButtons">
                <a onClick='copyStringToClipboard(document.getElementById("copyRaw").textContent);'><i class="material-icons copybtn">content_copy</i> </a>
                <a class="link1" href="/<?php echo ($id); ?>/raw"> Raw</a>
                <a class="link1" href="/<?php echo ($id); ?>/raw" download="<?php echo ($pastetitle); ?>"> Download</a>
                <?php if($mypaste):?>
                    <a class="link1" href="/delete:paste/<?php echo ($id); ?>"> Delete</a>
                <?php endif; ?>
            </div>
        </div>
    <?php endif; ?>
</div>

<?php tmpl("footer"); ?>