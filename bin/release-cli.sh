#!/usr/bin/env bash

set -euo pipefail
#########################
#COMMAND LINE INTERFACE
#########################

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
JONGO_MAVEN_OPTIONS="--quiet --errors --batch-mode -P release"

function append_maven_options(){
    JONGO_MAVEN_OPTIONS="${JONGO_MAVEN_OPTIONS} ${1}"
}

function set_dry_run_mode(){
    local repo_dir="${1}"

    append_maven_options "-P test"
    update_origin_with_fake_remote "${repo_dir}"

    echo "[WARN] Script is running in dry run mode with fake remote origin"
    echo "[WARN] All new commits, branches and tags will be pushed to this fake remote"
    echo "[WARN] Cloned repository: ${repo_dir}"
}

function __main() {

    local dry_run=true
    local dirty=false
    local job=()

    while [[ $# -gt 0 ]]
    do
    key="$1"
    case $key in
        -g|--gpg-file)
            import_gpg "${2}"
            shift
            shift
        ;;
         -s|--settings-file)
            append_maven_options "--settings ${2}"
            shift
            shift
        ;;
        -b|--branch)
            readonly target_branch="$2"
            shift
            shift
        ;;
        -t|--tag)
            readonly tag="$2"
            shift
            shift
        ;;
        #Optional
        --dirty)
            readonly dirty=true
            shift
        ;;
        --debug)
            set -x
            shift
        ;;
        -d|--dry-run)
            readonly dry_run="$2"
            shift
            shift
        ;;
        *)
        job+=("$1")
        shift
        ;;
    esac
    done
    set -- "${job[@]}"

    echo "[INFO] Running job ${job}"
    source "${JONGO_BASE_DIR}/bin/lib/tasks.sh"

    echo "[INFO] Cloning repository..."
    local repo_dir=$(clone_repository)
    local script_branch="$(get_current_branch_name)"

    if [ "${dry_run}" = true ] ; then
        set_dry_run_mode "${repo_dir}"
    fi

    if [ "${dirty}" = false ] ; then
        trap clean_resources EXIT
    fi

    echo "[INFO] Maven options ${JONGO_MAVEN_OPTIONS}"
    pushd "${repo_dir}" > /dev/null
        case "${job}" in
        RELEASE_EARLY)
           create_early_release "${target_branch}"
        ;;
        RELEASE)
           create_release "${target_branch}"
        ;;
        RELEASE_HOTFIX)
           create_hotfix_release "${target_branch}"
        ;;
        DEPLOY)
           deploy "${tag}" "AB643632CF5E746D"
        ;;
        TEST_RELEASE_FLOW)
            source "${JONGO_BASE_DIR}/src/test/sh/test-tasks.sh"
            run_test_suite "${script_branch}"
        ;;
        *)
         echo -e "[ERROR] Unknown job ${job}"
         exit 1;
        ;;
        esac
    popd > /dev/null
}

__main "$@"