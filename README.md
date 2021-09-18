<p align="center"><img src="/frontend/src/assets/logo.png" width="200"></p>

Pastefy is an open source alternative to Gists or Pastebin.<br>
You want to share some code to your friends or just save it for yourself? Just paste it.

![Screenshot](https://i.ibb.co/THWmH7s/shot1341621013741.png)
## Features
- Raw-Preview
- Copy Button
- Fork
- An API ([Docs](https://github.com/interaapps/pastefy/wiki/API-v2))
- Log in
  - Folders 
  - See your created pastes
  - delete created pastes

# Overview
- [Screenshots](#Screenshots)
- [Deploy](#Deploy)
  - [Docker](#Deploy)
  - [Docker-Compose](#Docker-Compose)
  - [Custom Docker-Compose](#Custom-Docker-Compose)
  - [Kubernetes](#Kubernetes)
  - [Container-Less](#Container-Less)
- [Develop](#Develop)
- [API](#API)

# Screenshots
![](.github/screenshots/home.png)
![](.github/screenshots/paste.png)
![](.github/screenshots/new.png)
![](.github/screenshots/fullscreen.png)

## Deploy
### Docker
DockerHub: https://hub.docker.com/repository/docker/interaapps/pastefy/general
```bash
docker run -p 8080:80 \
      --HTTP_SERVER_PORT=80 \
      --HTTP_SERVER_CORS="*" \
      --DATABASE_DRIVER=mysql \
      --DATABASE_NAME=pastefy \
      --DATABASE_USER=pastefy \
      --DATABASE_PASSWORD=pastefy \
      --DATABASE_HOST=db \
      --DATABASE_PORT=3306 \
      --AUTH_PROVIDER=NONE \
      --INTERAAPPS_AUTH_KEY= \
      --INTERAAPPS_AUTH_ID= \
```

### Docker-Compose
https://github.com/interaapps/pastefy/blob/master/docker-compose.yml
```bash
git clone https://github.com/interaapps/pastefy.git
cd pastefy
docker-compose up
```

### Custom Docker-Compose (Using Docker-Hub)
https://pastefy.ga/Hj9N3bs2
```bash
wget https://pastefy.ga/Hj9N3bs2/raw -O docker-compose.yml
nano docker-compose.yml
docker-compose up
```

### Kubernetes
You can find an example kubernetes file here https://github.com/interaapps/pastefy/blob/master/deployment/prod.yaml. It's made to use with gitlab CI and cert-manager. With some changes you can use it.

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
You can find the docs of the Pastefy-Rest-APi here: [Github Wiki](https://github.com/interaapps/pastefy/wiki/API-v2)