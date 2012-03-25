#!/bin/bash

#https://raw.github.com/gtramontina/cloudbees-node

NODE_VERSION=v0.6.9
NODE_DIST_FILE='/private/jongo/nodejs-0.6.9-cloudbees.zip'
NODE_SOURCE_DIR='build/node'
NODE_INSTALL_DIR=$NODE_SOURCE_DIR'/installed'

# Plumbing...
exist_directory() {
    [ -d $1 ];
}
clone_node_from_github() {
    git clone https://github.com/joyent/node.git $NODE_SOURCE_DIR
    cd $NODE_SOURCE_DIR
    git checkout $NODE_VERSION
}
install_node() {
    mkdir -p $NODE_INSTALL_DIR
    PREFIX=$PWD/$NODE_INSTALL_DIR
    pushd $NODE_SOURCE_DIR
    ./configure --prefix=$PREFIX
    make install
    popd
}
is_command_in_path() {
    command -v $1 > /dev/null;
}
add_node_to_path() {
    export PATH=$PWD/$NODE_INSTALL_DIR/bin:${PATH}
}
install_npm() {
    curl http://npmjs.org/install.sh | clean=yes sh
}

# [ Start! ]
if [ ! -d $NODE_INSTALL_DIR/bin ]
then
    if [ -f $NODE_DIST_FILE ]
    then
     echo "Get nodejs $NODE_VERSION from $NODE_DIST_FILE archive"
     mkdir -p $NODE_INSTALL_DIR
     unzip -vd $NODE_INSTALL_DIR $NODE_DIST_FILE
    else
     echo "Build nodejs from sources"
     clone_node_from_github
     install_node
     install_npm
    fi
fi

add_node_to_path
node --version
npm --version


