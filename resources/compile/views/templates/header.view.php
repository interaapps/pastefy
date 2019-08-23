<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">

    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <link rel="shortcut icon" type="image/png" href="/assets/images/icon.png"> <meta name="theme-color" content="#333344" />
    <meta name="description" content="Pastefy is an Open Source self-hosted Pastebin/GithubGist alternative.">
    <meta property="og:site_name" content="Pastefy" />
    <meta property="og:locale" content="en_UK" />
    <meta property="og:type" content="website" />
    <meta property="og:title" content="Pastefy" />
    <meta property="og:description" content="Pastefy is an Open Source self-hosted Pastebin/GithubGist alternative." />
    <meta property="og:url" content="https://pastefy.ga" />
    <meta property="og:site_name" content="Pastefy" />
    <meta name="twitter:card" content="summary_large_image" />
    <meta name="twitter:description" content="Pastefy is an Open Source self-hosted Pastebin/GithubGist alternative." />
    <meta name="twitter:title" content="Pastefy" />

    <!-- <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/styles/default.min.css"> -->
    <link rel="stylesheet" href="https://indestructibletype.com/fonts/Jost.css" type="text/css" charset="utf-8" />
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link rel="stylesheet" href="/assets/css/app.css">

    <script src="/assets/js/app.js"></script>
    <script src="//cdnjs.cloudflare.com/ajax/libs/highlight.js/9.13.1/highlight.min.js"></script>

    <title>{{$title}} » Pastefy.ga</title>
</head>
<body>
<div id="nav">
    <div id="logo"><a href="/"><img src="/assets/images/logo.png" /></a></div>
    <div id="nav_menu">
        @if((\app\classes\User::usingIaAuth()))#
            @if((\app\classes\User::loggedIn()))#
                <a href="/pasteList" class="nav_link"><img style="border: solid {{ htmlspecialchars(\app\classes\User::getUserObject()->color) }} 2px;" id="profilepicture" src="{{ htmlspecialchars(\app\classes\User::getUserObject()->profilepic) }}" /></a>
            @else
                <a href="/pasteList" class="nav_link">Login</a>
            @endif
        @else
        @endif
    </div>
</div>
