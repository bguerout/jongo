#!/bin/sh

SITE_FOLDER=generated-site

stop_on_error() {
 if [ $? -ne 0 ] ; then
  echo "$1"
  exit 1
 fi
}

#Download node dependencies
npm install express@2.5.8 jake@0.2.18 jsdom@0.2.12 less@1.2.2 wrench@1.3.7
stop_on_error "Unable to install npm dependencies"


rm -rf ./$SITE_FOLDER
node node_modules/jake/bin/cli.js -f scripts/generate-site.js

echo "
Site has been generated into $SITE_FOLDER folder.
You can check it by starting node server and going to http://localhost:3000/$SITE_FOLDER"
