#!/bin/sh

set -e

#Download node dependencies
echo "Installing npm dependencies"
npm install

#Run jake on site branch
node $PWD/node_modules/jake/bin/cli.js -f scripts/generate-site.js

serve target/jongo_org_website