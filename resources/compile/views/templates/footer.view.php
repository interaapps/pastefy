
<div id="footer">
    <img height="50px" src="/assets/images/icon.png" />
    <div id="linksection">
        <?#
        global $ULOLE_CONFIG;
        ?>

        @if((isset($ULOLE_CONFIG->footer_links)))#
            @foreach(($ULOLE_CONFIG->footer_links as $name=>$page))#
                @if(($page == "__br__"))#
                    <br>
                @else
                    <a class="link1" href="{{$page}}">{{$name}}</a>
                @endif
            @endforeach
        @endif
    </div>

</div>

</body>
</html>
<script>


</script>