#!/usr/bin/env bash

set -euo pipefail

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
source "${JONGO_BASE_DIR}/bin/lib/common/mvn-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/git-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/gpg-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/logger.sh"
source "${JONGO_BASE_DIR}/bin/lib/release/tasks.sh"

function usage {
    echo "Usage: $0 [option...] {release|release_hotfix|test}"
    echo
    echo "Command line interface to build, package and deploy Jongo"
    echo "Note that by default all tasks are ran in dry mode. Set '--dry-run false' to run it for real. "
    echo
    echo "   --branch           The base branch to release"
    echo "   --tag              The tag used to deploy artifacts"
    echo "   --dry-run          Run task in dry mode. Nothing will be pushed nor deployed (default: true)"
    echo "   --gpg-file         Path to the GPG file used to sign artifacts"
    echo "   --maven-options    Maven options (eg. --settings /path/to/settings.xml)"
    echo "   --dirty            Do not clean generated resources during execution (eg. cloned repository)"
    echo "   --debug            Print all executed commands and run Maven in debug mode"
    echo
    echo "Usage examples:"
    echo ""
    echo " Release a new version from the master branch:"
    echo ""
    echo "      bash ./bin/cli.sh release --gpg-file /path/to/file.gpg --branch master"
    echo ""
    echo " Deploy a version from inside a docker container."
    echo ""
    echo "      docker build bin -t jongo-releaser && \\"
    echo "      docker run -it --volume /path/to/files:/opt/jongo/conf jongo-releaser \\"
    echo "         deploy \\"
    echo "        --maven-options \"--settings /opt/jongo/conf/settings.xml\" \\"
    echo "        --gpg-file /opt/jongo/conf/file.gpg \\"
    echo "        --tag 42.0.0"
}

function configure_dry_mode() {
    local repo_dir="${1}"
    configure_deploy_plugin_for_test ${JONGO_BASE_DIR}
    update_origin_with_fake_remote "${repo_dir}"
    log_warn "Script is running in dry mode."
}

function safeguard() {
    while true; do
        read -p "[WARN] Do you really want to run this task for real?" yn
        case $yn in
            [Yy]* ) break;;
            [Nn]* ) exit;;
            * ) echo "Please answer yes or no.";;
        esac
    done
}

function __main() {

    local dry_run=true
    local positional=()

    while [[ $# -gt 0 ]]
    do
    key="$1"
    case $key in
        --branch)
            local git_revision="$2"
            shift
            shift
        ;;
        --tag)
            local git_revision="$2"
            shift
            shift
        ;;
        --gpg-file)
            local -r gpg_keyname=$(import_gpg "${2}")
            log_info "GPG key ${gpg_keyname} imported from file ${2}"
            configure_maven_gpg_plugin "${gpg_keyname}"
            shift
            shift
        ;;
        --maven-options)
            append_maven_options "${2}"
            shift
            shift
        ;;
        --dirty)
            trap clean_resources EXIT
            log_warn "Dirty mode activated."
            shift
        ;;
        --debug)
            set -x
            readonly debug=true
            append_maven_options "-Dsurefire.printSummary=true"
            shift
        ;;
        -d|--dry-run)
            readonly dry_run="$2"
            shift
            shift
        ;;
        -?|--help)
            usage
            exit 0;
        ;;
        *)
        positional+=("$1")
        shift
        ;;
    esac
    done
    set -- "${positional[@]}"

    local repo_dir=$(clone_repository "${dry_run}")
    pushd "${repo_dir}" > /dev/null

        local task="${1}"
        if [[ "${dry_run}" = true ]] ; then
            configure_dry_mode "${repo_dir}"
        else
            if [[ ${git_revision} = *"-early-"* ]]  || [[ "${task}" == "release_early" ]]; then
                configure_deploy_plugin_for_early
            fi
            safeguard
        fi

        case "${task}" in
            test)
                source "${JONGO_BASE_DIR}/bin/test/test-tasks.sh"
                run_test_suite "${git_revision}"
            ;;
            release_early)
                create_early_release "${git_revision}"
            ;;
            release)
                create_release "${git_revision}"
            ;;
            release_hotfix)
                create_hotfix_release "${git_revision}"
            ;;
            deploy)
                deploy "${git_revision}"
            ;;
            *)
             log_error "Unknown task '${task}'"
             usage
             exit 1;
            ;;
        esac
    popd > /dev/null
}

__main "$@"