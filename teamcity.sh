#!/bin/bash

set -e

install_globals() {
  npm install -g snyk yarn
}

snyk_test() {
  snyk test --org=guardian --file=package.json
  snyk test --org=guardian --file=build.sbt
}

js_pluto_lambda(){
  pushd pluto-message-ingestion
  yarn install
  yarn run build
  popd
}

js_deps() {
  yarn install --force --non-interactive
}

js_test() {
  yarn lint
  yarn test
}

js_build() {
  yarn run build
}

main() {
  install_globals
  snyk_test
  js_pluto_lambda
  js_deps
  js_test
  js_build
}

main
