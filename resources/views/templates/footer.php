
<div id="footer">
    <img height="50px" src="/assets/images/icon.png" />
    <div id="linksection">
        <?php
        global $ULOLE_CONFIG;
        ?>

        <?php if(isset($ULOLE_CONFIG->footer_links)):?>
            <?php foreach($ULOLE_CONFIG->footer_links as $name=>$page):?>
                <?php if($page == "__br__"):?>
                    <br>
                <?php else: ?>
                    <a class="link1" href="<?php echo ($page); ?>"><?php echo ($name); ?></a>
                <?php endif; ?>
            <?php endforeach; ?>
        <?php endif; ?>
    </div>

</div>

</body>
</html>
<script>


</script>