#!/bin/bash

NODE_DIST=node-v0.8.16-linux-x64
NODE_INSTALL_DIR=target/$NODE_DIST
NODE_BIN_FILE=$NODE_DIST.tar.gz

stop_on_install_error() {
 if [ $? -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "$1"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  exit 1
 fi
}
if [ -d $NODE_INSTALL_DIR ]
then
    export PATH=$PWD/$NODE_INSTALL_DIR/bin:${PATH}
    echo "*****************************************************"
    echo "nodejs is already available at $PWD/$NODE_INSTALL_DIR"
    echo "*****************************************************"
else
    echo "Installing $NODE_DIST"
    wget --no-verbose -O $NODE_BIN_FILE https://github.com/CloudBees-community/node-clickstack/blob/master/$NODE_BIN_FILE?raw=true
    stop_on_install_error "Unable to download $NODE_BIN_FILE"
    mkdir target
    tar xf $NODE_BIN_FILE -C target
    rm $NODE_BIN_FILE
    export PATH=$PWD/$NODE_INSTALL_DIR/bin:${PATH}
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


