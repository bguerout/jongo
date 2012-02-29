#!/bin/bash 
 echo This script must on gh-pages branch 
 git rm -r --ignore-unmatch * 
 mv gh-pages/* ./ 
 rm -rf gh-pages/ 
 rm /scratch/hudson/workspace/release-site/jongo/apply_changes.sh
 git add .