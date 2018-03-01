#!/usr/bin/env bash

set -euo pipefail
#########################
#COMMAND LINE INTERFACE
#########################

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
source "${JONGO_BASE_DIR}/bin/lib/_mvn.sh"
source "${JONGO_BASE_DIR}/bin/lib/repository-tools.sh"

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
            readonly branch="$2"
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

    source "${JONGO_BASE_DIR}/bin/lib/log.sh"
    [[ "${dirty}" = false ]] && trap clean_resources EXIT || log_warn "Dirty mode activated."
    [[ "${dry_run}" = true ]] &&  append_maven_options "-P test" && log_warn "Script is running in dry mode"

    source "${JONGO_BASE_DIR}/bin/lib/tasks.sh"
    local target_branch="${branch:-$(get_current_branch_name)}"

    log_info "***************************************************************************************"
    log_info "* Running job ${job} with parameters:"
    log_info "*   Maven options '$(get_maven_options)'"
    log_info "*   Target Branch '${target_branch}'"
    log_info "***************************************************************************************"

    function prepare_repository {
        local repo_dir=$(clone_repository "https://github.com/bguerout/jongo.git")
        if [ "${dry_run}" = true ] ; then
            update_origin_with_fake_remote "${repo_dir}"
        fi
        echo "${repo_dir}"
    }

    case "${job}" in
        RELEASE_EARLY)
            local repo_dir=$(prepare_repository)
            pushd "${repo_dir}" > /dev/null
                create_early_release "${target_branch}"
            popd > /dev/null
        ;;
        RELEASE)
            local repo_dir=$(prepare_repository)
            pushd "${repo_dir}" > /dev/null
                create_release "${target_branch}"
            popd > /dev/null
        ;;
        RELEASE_HOTFIX)
            local repo_dir=$(prepare_repository)
            pushd "${repo_dir}" > /dev/null
                create_hotfix_release "${target_branch}"
            popd > /dev/null
        ;;
        DEPLOY)
            local repo_dir=$(prepare_repository)
            pushd "${repo_dir}" > /dev/null
                deploy "${tag}" "AB643632CF5E746D"
            popd > /dev/null
        ;;
        TEST_RELEASE_FLOW)
            local repo_dir=$(prepare_repository)
            pushd "${repo_dir}" > /dev/null
                source "${JONGO_BASE_DIR}/src/test/sh/test-tasks.sh"
                run_test_suite "${target_branch}"
            popd > /dev/null
        ;;
        *)
         log_error "Unknown job ${job}"
         exit 1;
        ;;
    esac
}

__main "$@"