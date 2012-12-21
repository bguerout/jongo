#!/bin/bash

NODE_DIST=node-v0.8.16-linux-x64
NODE_BIN_FILE=$NODE_DIST.tar.gz

stop_on_download_error() {
 if [ $? -ne 0 ] ; then
  echo "$1"
  exit 1
 fi
}
if [ ! $NODE_DIST ]
then
    echo "Installing $NODE_DIST"
    wget --no-verbose -O $NODE_BIN_FILE https://github.com/CloudBees-community/node-clickstack/blob/master/$NODE_BIN_FILE?raw=true
    stop_on_download_error "Unable to download $NODE_BIN_FILE"
    tar xf $NODE_BIN_FILE
    export PATH=$PWD/$NODE_DIST/bin:${PATH}
    echo "nodejs $(node -v) has been installed."

    echo "Installing npm"
    pushd $NODE_DIST
    curl https://npmjs.org/install.sh | clean=yes sh
    stop_on_download_error "Unable to download npm"
    popd
    echo "npm $(npm -v) has been installed."
else
    export PATH=$PWD/$NODE_DIST/bin:${PATH}
fi


