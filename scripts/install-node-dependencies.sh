#!/bin/bash

NODE_VERSION=0.8.17
NODE_INSTALL_DIR=target/node-v$NODE_VERSION-linux-x64
NODE_BIN_FILE=node-$NODE_VERSION.tar.gz

stop_on_install_error() {
 if [ $? -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "$1"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
 fi
 exit 1
}

if [ -d $NODE_INSTALL_DIR ]
then
    export PATH=$PWD/$NODE_INSTALL_DIR/bin:${PATH}
    echo "nodejs is already available at $PWD/$NODE_INSTALL_DIR"
else
    echo "Installing nodejs $NODE_VERSION"
    wget --no-verbose -O $NODE_BIN_FILE http://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.gz
    stop_on_install_error "Unable to download $NODE_BIN_FILE"
    mkdir target
    tar xf $NODE_BIN_FILE -C target
    rm $NODE_BIN_FILE
    export PATH=$PWD/$NODE_INSTALL_DIR/bin:${PATH}
    node -v
    stop_on_install_error "Unable to install node"
    echo "*************************************"
    echo "nodejs $(node -v) has been installed."
    echo "*************************************"

    echo "Installing npm"
    pushd $NODE_INSTALL_DIR
    curl https://npmjs.org/install.sh | clean=yes sh
    stop_on_install_error "Unable to download npm"
    popd
    echo "*********************************"
    echo "npm $(npm -v) has been installed."
    echo "*********************************"

    #Download node dependencies
    echo "Installing npm dependencies"
    npm install express@3.0.5 jake@0.5.8 jsdom@0.3.3 less@1.3.1 wrench@1.4.4
    stop_on_install_error "Unable to install npm dependencies"
fi


