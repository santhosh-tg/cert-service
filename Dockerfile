FROM openjdk:8-jre-alpine
MAINTAINER "S M Y ALTAMASH <smy.altamash@gmail.com>"
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && apk --no-cache add chromium \
    && mkdir -p /home/sunbird/
ADD fonts /usr/share
RUN mkdir -p /usr/share/fonts/truetype/sunbird-fonts/
RUN find $PWD/fonts/ -name "*.ttf" -exec install -m644 {} /usr/share/fonts/truetype/sunbird-fonts/ \; || return 1
RUN apk --no-cache add msttcorefonts-installer fontconfig \
    && apk install -m644 \
    && update-ms-fonts \
    && fc-cache -f
ADD ./cert-service-1.0.0-dist.zip /home/sunbird/
RUN unzip /home/sunbird/cert-service-1.0.0-dist.zip -d /home/sunbird/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/
CMD java  -cp '/home/sunbird/cert-service-1.0.0/lib/*' play.core.server.ProdServerStart  /home/sunbird/cert-service-1.0.0
