<?#
$light = false;
if (isset($_GET["light"]))
    $light = true;
?>

@if(($light))#
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.10/styles/arduino-light.min.css">
@else
<link rel="stylesheet" href="/assets/css/highlight.css">
@endif

<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

{{ isset($_GET["nofont"]) ? "" : '<link rel="stylesheet" href="https://indestructibletype.com/fonts/Jost.css" type="text/css" charset="utf-8" />' }}

<script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/highlight.min.js"></script>


<script>hljs.initHighlightingOnLoad();</script>
<div id="code">
    <h2>{[{$paste["title"]}]}</h2>
    <pre><code id="copyRaw">{[{$paste["content"]}]}</pre></code>
    <div id="toolbar">
        <a id="copyBtn" onClick='copyStringToClipboard(document.getElementById("copyRaw").textContent);'><i class="material-icons">content_copy</i></a> 
        <a target="_blank" href="https://pastefy.ga/{[{ $paste["id"] }]}/raw">Raw</a> 
        <a target="_blank" id="powered_by" href="https://pastefy.ga/{[{ $paste["id"] }]}">Powered by <img id="pastefy_logo" src="/assets/images/logo.png" ></a>
    </div>
</div>
<style>
    * {
        font-family: Jost, sans-serif;
    }

    h2 {
        @if(($light))#
            color: #323232;
        @else
            color: #FFFFFF;
        @endif
        font-size: 18px;
        font-weight: 500;
    }

    #code {

        @if(($light))#
            background: #FFFFFF;
            color: #454545;
        @else
            background: #262b39;
            color: #FFFFFF;
        @endif

        border-radius: 10px;
        padding: 10px 20px;
        display: block;
        margin-left: auto;
        margin-right: auto;
        margin-top: 20px;
        font-size: 14px;
        margin-bottom: 20px;
        -webkit-box-shadow: 0px 1px 19px 0px rgba(0,0,0,0.15);
        -moz-box-shadow: 0px 1px 19px 0px rgba(0,0,0,0.15);
        box-shadow: 0px 1px 19px 0px
        rgba(0,0,0,0.15);
        box-sizing: border-box;
    }

    #toolbar {
        display: flex;
    }

    #toolbar a, #toolbar a:link, #toolbar a:visited {
        @if(($light))#
            color: #434343;
        @else
            color: #FFFFFF;
        @endif
        line-height: 20px;
        font-size: 15px;
        text-decoration: none;
        vertical-align: middle;
    }
    
    #copyBtn {
        margin-right: 10px;
    }

    #copyBtn i {
        font-size: 20px;
        cursor: pointer;
        vertical-align: middle;
    }

    #powered_by, #powered_by:link, #powered_by:visited {
        float: right;
        margin-left: auto;
    }

    #pastefy_logo {
        height: 20px;
        margin-left: 5px;
        vertical-align: middle;
    }
</style>

<script>
function copyStringToClipboard(str) {
    var el = document.createElement('textarea');
    el.value = str;
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
 }
</script>