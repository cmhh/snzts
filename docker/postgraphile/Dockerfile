FROM ubuntu:20.04

ARG NODE_VERSION=14.17.5

ENV DEBIAN_FRONTEND=noninteractive

RUN  apt-get update && apt-get -y dist-upgrade && \
  apt-get install -y --no-install-recommends curl ca-certificates xz-utils && \
  apt-get clean && \
  rm -rf /var/lib/apt/lists/* 

RUN mkdir -p /usr/local/node && \
  curl -s https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.xz | tar xJvf - --strip-components=1 -C /usr/local/node && \
  ln -s /usr/local/node/bin/node /usr/local/bin/node && \
  ln -s /usr/local/node/bin/npm /usr/local/bin/npm && \
  ln -s /usr/local/node/bin/npx /usr/local/bin/npx && \
  npm install -g postgraphile

EXPOSE 5000

CMD /usr/local/node/bin/postgraphile \
  -c "${DB}" \
  --enhance-graphiql \
  --retry-on-init-fail \
  --host "0.0.0.0" \
  --graphql ${BASE}/graphql \
  --graphiql ${BASE}/graphiql && \
  tail -f /dev/null