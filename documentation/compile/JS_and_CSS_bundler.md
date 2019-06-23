# Javascript and CSS bundler
If you don't want to use webpack with nodejs you can use compile.
To use it you have to go in to the folder resources/compile/js or ~/css and add a compile.json. In it you can add "compile" "rules".

#### Example (For javascript)
```json
{
    "assets/js/output.js": [ 
        "app.js"
    ]
}
```

Explaination
```
"assets/js/output.js": # This the output file (This will exported to public/assets/js/output.js) 
```

```
[ 
    "app.js" # Here are the files in an Array that are going to be compiled (You can compile multible into one ["app.js", "test.js"] -> will be compiled into one file)
]
```

In CSS it's the same but just with CSS files