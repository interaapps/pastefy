<?php tmpl("header", ["title"=>"Homepage"]); ?>
<div id="web_contents">
    <a class="submitbutton" id="createnewkey">Create new API-Key</a>
    <h1 id="page_title">Pastefy Developer Console</h1><br>
    <div id="keys">

    </div>
</div>
<script>
    $("#createnewkey").click(function(){
        Cajax.post("/dev/console/newkey").then(loadKeys).send();
    });



    function loadKeys(){
        Cajax.get("/dev/console/ls").then(function(res){
            $("#keys").html("");
            parsed = JSON.parse(res.responseText);
            for (var obj in parsed) {
                obj = parsed[obj];
                $("#keys").append(
                    $n("div").addClass("apikey").append(
                        $n("a").text(obj.id).append(
                        $n("a").attr("key-id", obj.id).addClass("deletekey").text("Delete")
                        )
                    )
                );
            }
            $(".deletekey").click(function(){
                Cajax.post("/dev/console/deletekey", {
                    id: $(this).attr("key-id")
                }).then(loadKeys).send();
            });
            showSnackBar("loaded");
        }).catch(function(){
            showSnackBar("error while loading");
        }).send();
    }

    loadKeys();
</script>
<style>
    #createnewkey {
        float: right;
    }

    .apikey {
        background: #FFFFFF11;
        border-radius: 4px;
        padding: 5px;
        margin-bottom: 5px;
    }

    .deletekey {
        background: #ee4444;
        border-radius: 3px;
        padding: 1px 10px;
        float: right;
        cursor: pointer;
    }

    @media screen and (max-width: 720px) {
        #createnewkey {
            float: inherit;
        }

        #page_title {
            display: none;
        }
    }
</style>
<?php tmpl("footer"); ?>