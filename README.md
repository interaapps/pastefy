<p align="center"><img src="/public/assets/images/logo.png" width="200"></p>



#  Pastefy 4.3
Pastefy is an Open Source self-hosted Pastebin/GithubGist alternative.

# Contributing
Feel free to improve Pastefy by adding stuff to the code or writing some Github-Issues.

To change something to the code you'll need:
- Knowledge about PHP
- Can work with the [UloleFramework](https://github.com/interaapps/ulole-framework)
- PHP 7.3 CLI installed on your system

We are using ulole-framework compile. You need the modules `php uppm.php install` You have to type `php cli compile`  in a terminal to compile the resources in the `resource/compile` folder. To test your code you have to start the ulole-framework server `php cli server`. We do not support composer dependencies! Just [UPPM](https://github.com/interaapps/uppm) ones

#### Deno CLI
```bash
deno run --allow-net --allow-read https://pastefy.ga/cli.js (filename)
or
deno run -A https://pastefy.ga/cli.js (filename)
```

# Updates

## v4

```
Changed Ulole-Framework version and design
```

## 4.2
```
Added short paste and changed some design-details!
```

## 4.1
```
Updated ULOLE (Migration Support for databases!)
```

## 4
```
Completly recoded and redesigned. (+ Bug fixes)
```

## v3
### 3.1
```
Added Folder System, List of own pastes. And more. 
```

### 3.0
```
Added encryption, added Password encryption, InteraApps Account support (Not for selfhoster). 
```

## v2
### 2.1
```
Added more security for pastes, password-save, made System cleaner and better. Improved some design things!
```
