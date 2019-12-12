FROM openjdk:8-jre-alpine
MAINTAINER "S M Y ALTAMASH <smy.altamash@gmail.com>"
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && apk --no-cache add chromium \
    && mkdir -p /home/sunbird/
RUN apk --no-cache add msttcorefonts-installer fontconfig \
    && update-ms-fonts \
    && fc-cache -f
RUN wget https://github.com/google/fonts/archive/master.tar.gz -O gf.tar.gz
RUN tar -xf gf.tar.gz
RUN mkdir -p /usr/share/fonts/truetype/google-fonts
RUN find $PWD/fonts-master/ -name "*.ttf" -exec install -m644 {} /usr/share/fonts/truetype/google-fonts/ \; || return 1
RUN rm -f gf.tar.gz
RUN fc-cache -f && rm -rf /var/cache/*
ADD ./cert-service-1.0.0-dist.zip /home/sunbird/
RUN unzip /home/sunbird/cert-service-1.0.0-dist.zip -d /home/sunbird/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/
CMD java  -cp '/home/sunbird/cert-service-1.0.0/lib/*' play.core.server.ProdServerStart  /home/sunbird/cert-service-1.0.0