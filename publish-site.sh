#!/bin/sh

stop_on_error() {
 if [ $? -ne 0 ] ; then
  echo "$1"
  exit 1
 fi
}

echo "Running publish script with dry=$DRY_RUN and commit message=$COMMIT_MESSAGE"

#Clean
rm -rf jongo/ node*

#Install node
source /private/jongo/install-node.sh
stop_on_error "Unable to install node"

#Download node dependencies
npm install express@2.5.8 jake@0.2.18 jsdom@0.2.12 less@1.2.2 wrench@1.3.7
stop_on_error "Unable to install npm dependencies"
export NODE_PATH=$PWD/node_modules

#Clone repository and checkout site branch
rm -rvf jongo/
git --version
git clone git@github.com:bguerout/jongo.git
cd jongo
git checkout site

#Run jake on site branch
node $NODE_PATH/jake/bin/cli.js -f scripts/generate-site.js
stop_on_error "Jake script has failed"

#Trash current files and apply new sources
git checkout -b gh-pages origin/gh-pages
git rm -r --ignore-unmatch * && mv generated-site/* ./ && rm -rf generated-site/ && git add ./ && git status
stop_on_error "Unable to apply sources from site branch on gh-pages branch"

if [ "$DRY_RUN" = "false" ]; then
 echo "Pushing site to gh-pages"
 git commit -m "$COMMIT_MESSAGE" && git push
else
 echo "Script runs in DRY mode, nothing has been pushed." 
fi
