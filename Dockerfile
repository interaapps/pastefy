FROM webdevops/php-apache:7.3

COPY --chown=application . /app

ENV WEB_DOCUMENT_ROOT=/app/public