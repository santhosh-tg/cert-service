FROM openjdk:8-jre-alpine
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird/
RUN apk --no-cache add msttcorefonts-installer fontconfig \
    && update-ms-fonts \
    && fc-cache -f
ADD ./cert-service-1.0.0-dist.zip /home/sunbird/
RUN unzip /home/sunbird/cert-service-1.2.0-dist.zip -d /home/sunbird/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/

CMD java  -cp '/home/sunbird/cert-service-1.2.0/lib/*' play.core.server.ProdServerStart  /home/sunbird/cert-service-1.2.0