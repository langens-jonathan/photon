FROM clojure:lein-2.7.1

MAINTAINER Jonathan Langens <flowofcontrol@gmail.com>

ADD . /app

WORKDIR /app

CMD lein ring server-headless