FROM webdevops/php-nginx:7.3


COPY --chown=application . /app
WORKDIR /app

RUN php uppm.php install

ENV WEB_DOCUMENT_ROOT=/app/public
ENV WEB_DOCUMENT_INDEX=index.php

CMD php deployment/genenv.php && supervisord
