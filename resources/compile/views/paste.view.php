@template(("header", ["title"=>"Paste"]))!
<script>
hljs.initHighlightingOnLoad();
const embedLink = '<iframe width="100%" src="https://pastefy.ga/api/v1/embed/{{$id}}" onload="this.style.height = this.contentWindow.document.body.scrollHeight + \'px\';" frameborder="0" allowfullscreen></iframe>';
</script>
<div class="content">
    @if(($needspassword))#
        <div id="web_contents">
            <form action="/p/login/{{$id}}" method="POST">
                <a>This Paste is protected with a password!</a>
                <input class="titleinput" type="text" name="password" placeholder="Password">
                <input class="submitbutton" type="submit" name="sub" value="Send">
            </form>
        </div>
    @else
        <div class="pastecode" id="paste_code_container">
            <h2 id="paste_title" style="font-weight: normal">{{$pastetitle}}</h2>
            <div id="copyRaw"><pre><code>{{$code}}</code></pre></div>
            <div id="pasteButtons">
                <a onClick='showSnackBar("Copied"); copyStringToClipboard(document.getElementById("copyRaw").textContent);'><i class="material-icons copybtn">content_copy</i> </a>
                <a class="link1" href="/{{$id}}/raw"> Raw</a>
                <a class="link1" href="/{{$id}}/raw" download="{{$pastetitle}}"> Download</a>
                <a onClick='showSnackBar("Copied embed code"); copyStringToClipboard(embedLink);' class="link1"> Embed</a>
                @if(($mypaste))#
                    <a class="link1" href="/delete:paste/{{$id}}"> Delete</a>
                @endif
            </div>
        </div>
        <div class="pastecode" id="language_feature_container" style="display: none;"></div>
    @endif
</div>

<script>

if ($("#paste_title").text().includes(".")) {
    var pasteTitleComponents = $("#paste_title").text().split(".");
    var ending = pasteTitleComponents[pasteTitleComponents.length-1];
    var replacements = {
        "js": "javascript",
        "md": "markdown",
        "sh": "shell",
        "html": "xml",
        "htaccess": "apache",
        "c": "objectivec",
        "hack": "php",
        "coffee": "coffeescript",
        "c++": "cpp",
        "kotlin": "java",
        "kt": "java"

    };
    for (replace  in replacements)
        ending = ending.replace(replace, replacements[replace]);
    
    if (hljs.listLanguages().includes(ending)) {
        $("#copyRaw code").getFirstElement().className = "hljs language-"+ending;
        //$("#copyRaw code").addClass("language-html", ending);
    }
}

var language = "text";

var markdowned = false;

function setLanguageFeatured(lang){
    $("#paste_title").append("<span class='language_featured'>"+lang+"</span>");
    language = lang;
}

function refreshFeaturedLanguage(){
    const container = $("#language_feature_container");
    container.show();

    if (language = "markdown" && !markdowned) {
        Cajax.post("/api/v1/language/markdown", {markdown: $("#copyRaw").text()}).then(function(response){
            const parsed = JSON.parse(response.responseText);
            container.html(parsed.out);
            hljs.initHighlighting.called = false;
            hljs.initHighlighting();
        }).send();
        markdowned = true;
    }
}

addEventListener('load', function() {
    if ($("code").getFirstElement().classList.contains("markdown") || $("code").getFirstElement().classList.contains("language-markdown")) {
        setLanguageFeatured("markdown");
    }


    $(".language_featured").click(function(){
        refreshFeaturedLanguage();
    });



    if (window.location.hash == "#preview")
        $(".language_featured").getFirstElement().click();
    else if (window.location.hash == "#md_preview") {
        setLanguageFeatured("markdown");
        refreshFeaturedLanguage();
    } else if (window.location.hash == "#only_md_preview") {
        setLanguageFeatured("markdown");
        refreshFeaturedLanguage();
        $("#paste_code_container").hide();
    }
});

</script>


<style>
    .language_featured {
        background: #00000022;
        border-radius: 4px;
        margin-left: 14px;
        padding: 0px 14px;
        cursor: pointer;
        user-select: none;
    }

    #language_feature_container {
        padding: 6px 15px;
    }

    #language_feature_container code {
        background: #32323296;
        margin-top: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
    }

    li {
        margin-left: 20px;
    }
</style>

@template(("footer"))!