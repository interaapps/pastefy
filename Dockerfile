FROM webdevops/php-apache:7.3


COPY --chown=application . /app
WORKDIR /app

RUN php genenv.php
RUN php uppm.php install

ENV WEB_DOCUMENT_ROOT=/app/public