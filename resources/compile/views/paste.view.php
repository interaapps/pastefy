@template(("header", ["title"=>"Paste"]))!
<script>hljs.initHighlightingOnLoad();</script>
<div class="content">
    @if(($needspassword))#
        <div id="web_contents">
            <form action="/p/login/{{$id}}" method="POST">
                <a>This Paste is protected with a password!</a>
                <input class="titleinput" type="text" name="password" placeholder="Folder">
                <input class="submitbutton" type="submit" name="sub" value="Send">
            </form>
        </div>
    @else
        <div id="pastecode">
            <h2 style="font-weight: normal">{{$pastetitle}}</h2>
            <div id="copyRaw"><pre><code>{{$code}}</code></pre></div>
            <div id="pasteButtons">
                <a onClick='copyStringToClipboard(document.getElementById("copyRaw").textContent);'><i class="material-icons copybtn">content_copy</i> </a>
                <a class="link1" href="/{{$id}}/raw"> Raw</a>
                <a class="link1" href="/{{$id}}/raw" download="{{$pastetitle}}"> Download</a>
                @if(($mypaste))#
                    <a class="link1" href="/delete:paste/{{$id}}"> Delete</a>
                @endif
            </div>
        </div>
    @endif
</div>

@template(("footer"))!