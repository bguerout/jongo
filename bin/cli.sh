#!/usr/bin/env bash

set -euo pipefail
#########################
#COMMAND LINE INTERFACE
#########################

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
source "${JONGO_BASE_DIR}/bin/lib/common/mvn-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/git-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/gpg-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/logger.sh"

function __main() {

    local dry_run=true
    local dirty=false
    local debug=false
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
        -s|--settings-security)
            append_maven_options "-Dsettings.security=${2}"
            shift
            shift
        ;;
        -b|--branch)
            local -r branch="$2"
            shift
            shift
        ;;
        -t|--tag)
            local -r tag="$2"
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
            readonly debug=true
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

    source "${JONGO_BASE_DIR}/bin/lib/release/tasks.sh"

    local target_branch="${branch:-$(get_current_branch_name)}"
    [[ "${dry_run}" = true ]] &&  append_maven_options "-P test" && log_warn "Script is running in dry mode"
    [[ "${debug}" = false ]] &&  append_maven_options "--quiet"

    function before_task {
        local repo_dir=$(clone_repository "https://github.com/bguerout/jongo.git ")

        [[ "${dirty}" = false ]] && trap clean_resources EXIT || log_warn "Dirty mode activated."
        [[ "${dry_run}" = true ]] &&  update_origin_with_fake_remote "${repo_dir}"

        log_info "***************************************************************************************"
        log_info "* Running task ${job} with parameters:"
        log_info "*   Dry mode '${dry_run}'"
        log_info "*   Maven options '$(get_maven_options)'"
        log_info "*   Target Branch '${target_branch}'"
        log_info "*   Repository '${repo_dir}'"
        log_info "***************************************************************************************"

        pushd "${repo_dir}" > /dev/null
    }

    function after_task {
        popd > /dev/null
    }

    case "${job}" in
        RELEASE_EARLY)
            before_task
                create_early_release "${target_branch}"
            after_task
        ;;
        RELEASE)
            before_task
                create_release "${target_branch}"
            after_task
        ;;
        RELEASE_HOTFIX)
            before_task
                create_hotfix_release "${target_branch}"
            after_task
        ;;
        DEPLOY)
            before_task
                deploy "${tag}" "AB643632CF5E746D"
            after_task
        ;;
        TEST_RELEASE_FLOW)
            before_task
                source "${JONGO_BASE_DIR}/src/test/sh/test-tasks.sh"
                run_test_suite "${target_branch}"
            after_task
        ;;
        TEST)
            _mvn clean verify
        ;;
        *)
         log_error "Unknown job ${job}"
         exit 1;
        ;;
    esac
}

__main "$@"