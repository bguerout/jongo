#!/usr/bin/env bash

set -euo pipefail

readonly SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

bash "${SCRIPT_DIR}/compatibility/jackson-versions-tests.sh"
bash "${SCRIPT_DIR}/compatibility/mongodb-versions-tests.sh"
