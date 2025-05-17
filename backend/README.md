# Pastefy Backend

## Querying pastes

### HTTP
```
/paste?filter[visibility]=PUBLIC
```

### More complex queries
```js
/paste?filters={"visibility": "PUBLIC"}
```


### JS
```js
paste.getPastes({
    $or: [
        {"visibility": "PUBLIC"},
        {"visibility": "PRIVATE"}
    ]
})
```