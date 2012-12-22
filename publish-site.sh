#!/bin/sh

GENERATED_SITE_DIR=target/jongo_org_website

stop_on_error() {
 if [ $? -ne 0 ] ; then
  echo "$1"
  exit 1
 fi
}

echo "Running publish script with dry=$DRY_RUN and commit message=$COMMIT_MESSAGE"

#Install node
source scripts/install-node-dependencies.sh
stop_on_error "Unable to install node or a dependency"

#Run jake on site branch
node $PWD/node_modules/jake/bin/cli.js -f scripts/generate-site.js
stop_on_error "Jake script has failed"

#Trash current files and apply new sources
git checkout gh-pages
git rm -r --ignore-unmatch * && mv $GENERATED_SITE_DIR/* ./ && rm -rf $GENERATED_SITE_DIR && git add ./ && git status && git diff --staged
stop_on_error "Unable to apply sources from site branch on gh-pages branch"

if [ "$DRY_RUN" = "false" ]; then
 echo "Pushing site to gh-pages"
 git commit -m "$COMMIT_MESSAGE" && git push
else
 echo "Script runs in DRY mode, nothing has been pushed." 
fi
