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
ADD ./cert-service-1.0.0-dist.zip /home/sunbird/
RUN unzip /home/sunbird/cert-service-1.0.0-dist.zip -d /home/sunbird/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/
CMD [ \
  # Disable various background network services, including extension updating,
  #   safe browsing service, upgrade detector, translate, UMA
  "--disable-background-networking", \
  # Disable installation of default apps on first run
  "--disable-default-apps", \
  # Disable all chrome extensions entirely
  "--disable-extensions", \
  # Disable the GPU hardware acceleration
  "--disable-gpu", \
  # Disable syncing to a Google account
  "--disable-sync", \
  # Disable built-in Google Translate service
  "--disable-translate", \
  # Run in headless mode
  "--headless", \
  # Hide scrollbars on generated images/PDFs
  "--hide-scrollbars", \
  # Disable reporting to UMA, but allows for collection
  "--metrics-recording-only", \
  # Mute audio
  "--mute-audio", \
  # Skip first run wizards
  "--no-first-run", \
  # Disable sandbox mode
  # TODO get this running without it
  "--no-sandbox", \
  # Expose port 9222 for remote debugging
  #"--remote-debugging-port=9222", \
  # Disable fetching safebrowsing lists, likely redundant due to disable-background-networking
  "--safebrowsing-disable-auto-update" \
] && java  -cp '/home/sunbird/cert-service-1.0.0/lib/*' play.core.server.ProdServerStart  /home/sunbird/cert-service-1.0.0