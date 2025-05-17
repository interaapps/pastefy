# Pastefy Backend

# Minio & Elastic
When MinIO is enabled, elastic search is used to index the files. 
When not using both, the search and previews won't work.


## Querying pastes

### HTTP
```
/paste?filter[visibility]=PUBLIC
```

### More complex queries
```js
/paste?filters={"visibility": "PUBLIC", $or: [{userId: "34e2ddc"}, {userId: {"$ne": "d42ewde"}]}
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