#!/usr/bin/env bash

printf "\n\rSetting up Media Atom Maker dependencies... \n\r\n\r"

installed() {
  hash "$1" 2>/dev/null
}

nvm_installed() {
  if [ -d '/usr/local/opt/nvm' ] || [ -d "$HOME/.nvm" ]; then
    true
  else
    false
  fi
}

nvm_available() {
  type -t nvm > /dev/null
}

source_nvm() {
  if ! nvm_available; then
    [ -e "/usr/local/opt/nvm/nvm.sh" ] && source /usr/local/opt/nvm/nvm.sh
  fi
  if ! nvm_available; then
    [ -e "$HOME/.nvm/nvm.sh" ] && source $HOME/.nvm/nvm.sh
  fi
}

install_yarn() {
  if ! installed yarn; then
    echo 'Installing yarn'
    npm install -g yarn
  fi
}

install_deps_and_build() {
  yarn install
  printf "\n\Compiling Javascript... \n\r\n\r"
  yarn run build
}

main() {
  # if nvm is installed, install the node version required and use it
  if nvm_installed; then
    if ! nvm_available; then
      source_nvm
    fi
    nvm install
  fi

  install_yarn
  install_deps_and_build
  printf "\n\rDone.\n\r\n\r"
}

main
