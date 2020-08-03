# Create Paste `API KEY REQUIRED`

`API KEY REQUIRED!` Create an API-Key in the [Developer Console](/dev/console)



`POST` https://pastefy.ga/api/v1/create

## Post-Parameters `x-www-form-urlencoded`
| Parameter | required | description |
|--|--|--|
| apikey | YES | The apikey binds the pastes with the user-account of the api-key-creator. Open the [Developer Console](/dev/console) to get an api-key |
| content | YES | It is the content of the paste |
| title | OPTIONAL | Adding a title to the post. It also helps pastefy to detect the language by the extension |
| folder | OPTIONAL | Binds the paste to a folder that has been created by the apikey holder. |
| password | OPTIONAL | Protects the paste with a password. It also encrypts the paste |

## Return 

| key | type | description |
|--|--|--|
| done |boolean| Returns true if the creation succeeded |
| url |string(8)| Returns the ending of the url. The ID of the paste (For example: dcq0n99s) |


```json
{
    "done": true,
    "url": "AAAAAAAAA"
}
```

<div class="article_creator">
    <img src="https://accounts.interaapps.de/userpbs/JulianFun123.png" />
    <div>
        <a>Documentation by JulianFun123</a>
        <p>Develops at InteraApps</p>
    </div>
</div>