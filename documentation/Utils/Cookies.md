# Cookies

## Create a new cookie with the CookieBuilder
```php
use ulole\core\classes\util\cookies\CookieBuilder;
(new CookieBuilder("myKey"))->value("myValue")->time(CookieBuilder::HOUR*5)->build();
```

## Gets a Cookie

```php
use ulole\core\classes\util\cookies\Cookies;
Cookies::get("key");
```