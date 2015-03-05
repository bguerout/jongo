#!/bin/bash

NODE_VERSION=0.10.36

stop_on_install_error() {
 if [ $? -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "$1"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  exit 1
 fi
}

echo "Installing nodejs $NODE_VERSION"
curl https://raw.githubusercontent.com/creationix/nvm/v0.11.2/install.sh | bash
. "$HOME/.nvm/nvm.sh"
stop_on_install_error "nvm has not been installed"

nvm install $NODE_VERSION
nvm use $NODE_VERSION
node -v
stop_on_install_error "node has not been installed"

#Download node dependencies
echo "Installing npm dependencies"
npm install express@3.0.5 jake@0.5.8 jsdom@0.3.3 less@1.3.1 wrench@1.4.4
stop_on_install_error "Unable to install npm dependencies"



