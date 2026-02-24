#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/utils.sh"

function main() {
    unsupported "bson4jackson" "2.5.0"
    unsupported "bson4jackson" "2.6.0"
    unsupported "bson4jackson" "2.7.0"
    unsupported "bson4jackson" "2.8.0"
    unsupported "bson4jackson" "2.9.0"
    unsupported "bson4jackson" "2.9.1"
    unsupported "bson4jackson" "2.9.2" "$@"
    unsupported "bson4jackson" "2.11.0" "$@"
    unsupported "bson4jackson" "2.12.0" "$@"
    check_lib_compatibility "bson4jackson" "2.13.0" "$@"
    check_lib_compatibility "bson4jackson" "2.13.1" "$@"
    check_lib_compatibility "bson4jackson" "2.15.0" "$@"
    check_lib_compatibility "bson4jackson" "2.15.1" "$@"
    check_lib_compatibility "bson4jackson" "2.18.0" "$@"

}

main "$@"
