<p align="center">
    <a>
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="/.github/logo-white.svg">
        <source media="(prefers-color-scheme: light)" srcset="/.github/logo-black.svg" />
        <img src="/.github/logo-black.svg" width="200px" />
    </picture>
    </a>
</p>


<img src="./.github/screenshots/paste.png" width="100%" >

Pastefy is an open source alternative to Gists or Pastebin.<br>
You want to share some code to your friends or just save it for yourself? Just paste it.

Try out the public instance https://pastefy.app <br>
Visit the docs for everything around Pastefy here: https://docs.pastefy.app

## Features
- Raw-Preview
- Copy Button
- Fork
- An API ([Docs](https://docs.pastefy.app/api/))
  - [Javascript/Typescript](https://github.com/interaapps/js-api-clients?tab=readme-ov-file#pastefy)
  - [Java](https://github.com/interaapps/pastefy-java-apiclient)
  - [Go](https://github.com/interaapps/pastefy-go-api)
- Log in
  - Folders 
  - See your created pastes
  - delete created pastes
- Create paste with `curl -F f=@file.txt pastefy.app`
- Extensions:
  - VS-Code
  - Raycast
- Previews for (file extension)
  - **[See examples here](https://pastefy.app/EByZpNoS)**
  - Markdown (.md)
  - Mermaid (.mermaid or .mmd)
  - SVG (.svg)
  - CSV (.csv)
  - GeoJSON (.geojson)
  - Diff (.diff)
  - calendar (.ics)
  - regex (.regex)
  - [Asciinema recordings](https://docs.pastefy.app/features/integrations/asciinema.html) (.cast) [(Example)](https://pastefy.app/ar9ehz8w)

# Overview
- [Screenshots](#Screenshots)
- Deploy
  - [Docker](https://docs.pastefy.app/self-hosting/installation/docker.html)
  - [Docker-Compose](https://docs.pastefy.app/self-hosting/installation/docker-compose.html)
  - [Container-Less](#Container-Less)
- [Configuration](https://docs.pastefy.app/self-hosting/configuration.html)
- [Develop](#Develop)
- [API](https://docs.pastefy.app/api/)

# Screenshots
<img src="./.github/screenshots/home.png" width="46%" >
<img src="./.github/screenshots/paste.png" width="46%" >
<img src="./.github/screenshots/new.png" width="46%" >
<img src="./.github/screenshots/fullscreen.png" width="46%" >

## Deploy

See [Self-Hosting](https://docs.pastefy.app/self-hosting/index.html) for more options.

### Container-Less
```bash
git clone https://github.com/interaapps/pastefy.git
cd pastefy/frontend
npm run install
npm run build
cd ../backend
mvn clean package
cd ..
cp .env.example .env
nano .env
java -jar backend/target/backend.jar
```
Using intelliJ? Look at [Develop](#Develop)

## Configuration
See [Configuration](https://docs.pastefy.app/self-hosting/configuration.html) for all options.
### Adding login
You can choose between [INTERAAPPS](https://accounts.interaapps.de/developers) (best integration), [GOOGLE](https://support.google.com/cloud/answer/6158849?hl=en), [GITHUB](https://docs.github.com/en/developers/apps/building-oauth-apps/creating-an-oauth-app), [DISCORD](https://discord.com/developers/docs/topics/oauth2) or [TWITCH](https://dev.twitch.tv/docs/authentication) for the provider (You can use all of them at the same time).
```properties
OAUTH2_${provider}_CLIENT_ID=${client_id}
OAUTH2_${provider}_CLIENT_SECRET=${client_secret}
```
#### Example
```properties
OAUTH2_INTERAAPPS_CLIENT_ID=dan3q9n
OAUTH2_INTERAAPPS_CLIENT_SECRET=ASDFASDF
```


#### Custom OIDC Example
```properties
OAUTH2_CUSTOM_CLIENT_ID=CLIENT_ID
OAUTH2_CUSTOM_CLIENT_SECRET=SECRET
OAUTH2_CUSTOM_AUTH_ENDPOINT=https://accounts.interaapps.de/auth/oauth2
OAUTH2_CUSTOM_TOKEN_ENDPOINT=https://accounts.interaapps.de/api/v2/authorization/oauth2/access_token
OAUTH2_CUSTOM_USERINFO_ENDPOINT=https://accounts.interaapps.de/api/v2/oidc/userinfo
```

## Develop

#### Build frontend into the backend
```bash
# You might want to build the frontend
cd frontend
npm build prod
```

#### Frontend
Run the backend (On port 1337) and then go to the frontend and run
```bash
cd frontend
npm run serve
```

We are using IntelliJ Idea and Visual Studio code.

### API
You can find the docs of the Pastefy-Rest-APi here: [Docs](https://docs.pastefy.app/api/)


## Administration
If you want to give yourself the admin role, you have to log into your MySQL server and set `type` on your account to `ADMIN` in the `pastefy_users` table.

You'll find the admin panel under `https://YOUR_URL/admin`


## Extra Features

Read more here [Docs](https://docs.pastefy.app/features/index.html)

### Upload via Curl
```bash
curl -F f=@file.txt pastefy.app
```
### Asciinema support

configure: `nano ~/.config/asciinema/config`
```
[api]
url = https://pastefy.app
```

Using asciinema
```bash
asciinema rec test.cast
# ...
asciinema upload test.cast

# Authenticate via Pastefy
# Pastefy will request you to set the install-id via `echo YOUR_PASTEFY_API_KEY > ~/.config/asciinema/install-id`
asciinema auth

asciinema upload test.cast
```