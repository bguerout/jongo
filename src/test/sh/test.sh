#!/usr/bin/env bash
set -euo pipefail

echo "Running compatibility tests against bson4jackson versions"
bash src/test/sh/bson4jackson-versions-tests.sh

echo "Running compatibility tests against jackson versions"
bash src/test/sh/jackson-versions-tests.sh

echo "Running compatibility tests against MongoDB drivers and databases versions"
bash src/test/sh/mongodb-versions-tests.sh
