@template(("header", ["title"=>"Docs v1"]))!

<script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/highlight.min.js"></script>
<script>
    $(document).ready(function() {
        hljs.initHighlightingOnLoad();
    });
</script>

<div class="firstcontents" id="docsv1">
    
    <div id="docsv1sidenav">
        <h3>Docs v1</h3>
        @foreach(($pages as $page=>$link))#
            <a class="link_black" style="display:block" href="{{$link}}">{{$page}}</a>
        @endforeach
    </div>

    <div id="docsv1contents">
        {{ $doc }}
    </div>


</div>
<style>
#docsv1 {
    display: flex;
    padding: 20px 50px;
    margin-top: 60px;
    box-sizing: border-box;
    min-height: calc(100% - 100px);
    color: #FFFFFF;
    box-sizing: border-box;
}

#docsv1contents h1,
#docsv1contents h2,
#docsv1contents h3,
#docsv1contents h4,
#docsv1contents h5 {
  margin-bottom: 16px;
  margin-top: 16px;
  font-weight: normal;
  color: #FFFFFF;
}

#docsv1sidenav {
    background: #262b39;
    width: 300px;
    box-sizing: border-box;
    padding: 20px;
    display: inline-table;
    border-radius: 6px;
}

#docsv1contents {
    width: 100%;
    box-sizing: border-box;
    padding: 0px 40px;
}


#docsv1contents th {
  background: #F9F9F977;
}


#docsv1contents th, #docsv1contents td {
  border: solid 1px #434343;
  padding: 6px 10px;
}

#docsv1contents table {
  border-collapse: collapse;
}

#docsv1contents pre {
    background: #262b39;
    padding: 6px;
    border-radius: 6px;
}

#docsv1contents code {
    box-sizing: border-box;
    background: #262b39;
    font-size: 16px;
    padding: 2px 5px;
    border-radius: 6px;
}

.article_creator {
    background: #262b39;
    border-radius: 6px;
    margin-top: 40px;
    padding: 17px;
    box-sizing: border-box;
    display: flex;
    width: max-content;
    max-width: 100%;
}

.article_creator img {
    border-radius: 100%;
    width: 40px;
    height: 40px;
    vertical-align: top
}

.article_creator a {
    color: #FFFFFF;
}

.article_creator p {
    font-size: 14px;
    color: #FFFFFF;
}

.article_creator div {
  margin-left: 20px;
}


@media screen and (max-width: 720px) {

  .article_creator {
      display: block;
  }
  .article_creator div {
    margin-left: 0px;
  }

  #docsv1 {
    display: block;
  }

  #docsv1sidenav {
    width: 100%;
    display: block;
  }

  #docsv1contents {
    width: 100%;
  }
}




</style>
@template(("footer"))!