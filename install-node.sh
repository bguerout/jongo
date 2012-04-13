#!/bin/bash

#https://raw.github.com/gtramontina/cloudbees-node

NODE_VERSION=v0.6.15
NODE_DIST_FILE='/private/jongo/nodejs-0.6.15-cloudbees.zip'
NODE_SOURCE_DIR='node'

if [ -f $NODE_DIST_FILE ]
then
    echo "Unpacking nodejs $NODE_DIST_FILE archive"
    unzip $NODE_DIST_FILE
else
    echo "Building nodejs from sources"
    git clone https://github.com/joyent/node.git $NODE_SOURCE_DIR
    pushd $NODE_SOURCE_DIR
    git checkout $NODE_VERSION
    ./configure --prefix='installed'
    make install
    rm -rf out/
    popd
fi

zip -r nodejs-$NODE_VERSION.zip $NODE_SOURCE_DIR
export PATH=$PWD/$NODE_SOURCE_DIR/installed/bin:${PATH}
echo "nodejs $NODE_VERSION has been installed."

echo "Installing npm"
pushd $NODE_SOURCE_DIR
curl http://npmjs.org/install.sh | clean=yes sh
popd
echo "npm has been installed."

#Assertions
node --version
npm --version

