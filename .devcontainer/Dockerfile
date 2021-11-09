
FROM us-docker.pkg.dev/cookies-eng/public/base/codespaces:v1h

ENV SHELL=zsh \
    HOME=/home/dev \
    DEVCONTAINER=yes

USER dev
WORKDIR /home/dev

COPY defs/container.bazelrc /home/dev/bazelrc.remote

RUN mkdir -p $HOME/sdk-java \
    && cd $HOME/sdk-java \
    && git clone https://github.com/CookiesCo/sdk-java . \
    && ./gradlew --no-daemon build -x test \
    && cd $HOME \
    && rm -fr sdk-java;
