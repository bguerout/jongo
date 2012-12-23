#!/bin/sh

GENERATED_SITE_DIR=target/jongo_org_website
GHPAGES_REPO_DIR=target/gh-pages

stop_on_error() {
 if [ $? -ne 0 ] ; then
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "$1"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  exit 1
 fi
}

echo "***************************************************************************"
echo "Running publish script with dry=$DRY_RUN and commit message=$COMMIT_MESSAGE"
echo "***************************************************************************"

#Install node
source scripts/install-node-dependencies.sh
stop_on_error "Unable to install node or a dependency"

#Run jake on site branch
node $PWD/node_modules/jake/bin/cli.js -f scripts/generate-site.js
stop_on_error "Jake script has failed"

echo "******************************************"
echo "Applying site branch changes into gh-pages"
echo "******************************************"
rm -rf $GHPAGES_REPO_DIR
git clone --branch gh-pages git@github.com:bguerout/jongo.git $GHPAGES_REPO_DIR

pushd $GHPAGES_REPO_DIR
git rm -r --ignore-unmatch * && mv ../$GENERATED_SITE_DIR/* ./ && git add ./ && git status && git diff --staged
stop_on_error "Unable to apply sources from site branch on gh-pages branch"
if [ "$DRY_RUN" = "false" ]; then
 echo "************************"
 echo "Pushing site to gh-pages"
 echo "************************"
 git commit -m "$COMMIT_MESSAGE" && git push
else
 echo "Script runs in DRY mode, nothing has been pushed." 
fi
popd
