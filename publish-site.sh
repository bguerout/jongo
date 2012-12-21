#!/bin/sh

stop_on_error() {
 if [ $? -ne 0 ] ; then
  echo "$1"
  exit 1
 fi
}

echo "Running publish script with dry=$DRY_RUN and commit message=$COMMIT_MESSAGE"

#Install node
source scripts/install-node.sh
stop_on_error "Unable to install node"

#Download node dependencies
npm install express@3.0.5 jake@0.5.8 jsdom@0.3.3 less@1.3.1 wrench@1.4.4
stop_on_error "Unable to install npm dependencies"
export NODE_PATH=$PWD/node_modules

#Run jake on site branch
node $NODE_PATH/jake/bin/cli.js -f scripts/generate-site.js
stop_on_error "Jake script has failed"

#Trash current files and apply new sources
git checkout gh-pages
git rm -r --ignore-unmatch * && mv target/jongo_org_website/* ./ && rm -rf target/ && git add ./ && git status
stop_on_error "Unable to apply sources from site branch on gh-pages branch"

if [ "$DRY_RUN" = "false" ]; then
 echo "Pushing site to gh-pages"
 git commit -m "$COMMIT_MESSAGE" && git push
else
 echo "Script runs in DRY mode, nothing has been pushed." 
fi
