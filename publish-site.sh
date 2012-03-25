#!/bin/sh
echo This script must on gh-pages branch

#Install node and modules
source /private/jongo/install-node.sh	
npm install express@2.5.8 jake@0.2.18 jsdom@0.2.12 less@1.2.2 wrench@1.3.7
export NODE_PATH=$PWD/node_modules

#Clone repository and checkout site branch
rm -rvf jongo/
git --version
git clone git@github.com:bguerout/jongo.git
cd jongo
git checkout site

#Run jake on site branch
node $NODE_PATH/jake/bin/cli.js -f scripts/generate-site.js

#Trash current files and apply new sources contained in gh-pages folder
git checkout -b gh-pages origin/gh-pages
git rm -r --ignore-unmatch * && mv generated-site/* ./ && rm -rf generated-site/ && git add ./ && git status && git commit -m "$COMMIT_MESSAGE" && git push
