@template(("header", ["title"=>"My Pastes"]))!
<script>hljs.initHighlightingOnLoad();</script>
<div id="web_contents">

    <a class="flatbtn" onclick="$('#newfolderin').show();">New Folder</a>
        <a class="flatbtn" href="/#{{$id}}">New Paste</a>
        <div id="newfolderin">
            <form action="/create:folder" method="POST">
                <input type="hidden" name="parent" value="{{$id}}">
                <input type="hidden" name="camefrom" value="/folder/{{$id}}">
                <a>Create new folder</a>
                <input class="titleinput" type="text" name="name" placeholder="Folder">
                <input class="submitbutton" type="submit" name="sub" value="Send">
            </form>
        </div>
    <script>$('#newfolderin').hide();</script><br><br>

    <h2>Folder</h2>
    @foreach(($folder as $obj))#
        <a class="pasteList_paste_href" href="/folder/{{ $obj->id }}"><div class="pasteList_paste_main">    
            <p class="pasteList_paste_title">{{ $obj->name }}</p>
            <p class="pasteList_paste_date">{{ htmlspecialchars($obj->created) }}</p>
        </div>
        </a>
    @endforeach

    <h2>Pastes</h2>

    @foreach(($pastes as $obj))#
    <?#
    if ($obj->encrypted == 1) {
        $title = htmlspecialchars(\ulole\core\classes\util\secure\AES::decrypt($obj->title, $obj->id));
        $content = htmlspecialchars(($obj->password == "") ? \ulole\core\classes\util\secure\AES::decrypt($obj->content, $obj->id) : "This paste cannot be previewed. This paste is encrypted with a password.");
    } else {
        $title = htmlspecialchars($obj->title);
        $content = htmlspecialchars($obj->content);
    }
    #?>
        <a class="pasteList_paste_href" href="/{{ $obj->id }}">
            <div class="pasteList_paste_main">        
                <p class="pasteList_paste_title">{{ $title }}</p>
                <p class="pasteList_paste_content"><pre><code style="font-size: 10px; max-height: 370px; overflow-y: hidden;/*overflow-x: hidden;*/ ">{{ substr($content, 0, 1000) }}</code></pre></p>
                <p class="pasteList_paste_date">{{ htmlspecialchars($obj->created) }}</p>
            </div>
        </a>
    @endforeach
    @if(($myfolder))#
        <a href="/delete:folder/{{$id}}">Delete this folder</a>
    @endif
</div>


@template(("footer"))!