#!/usr/bin/env bash

set -euo pipefail

readonly JONGO_BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
source "${JONGO_BASE_DIR}/bin/lib/common/mvn-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/git-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/gpg-tools.sh"
source "${JONGO_BASE_DIR}/bin/lib/common/logger.sh"
source "${JONGO_BASE_DIR}/bin/lib/release/tasks.sh"

function safeguard() {
    local task="${1}"
    while true; do
        read -p "[WARN] Do you really want to run ${task} for real?" yn
        case $yn in
            [Yy]* ) break;;
            [Nn]* ) exit;;
            * ) echo "Please answer yes or no.";;
        esac
    done
}

function configure_dry_mode() {
    local repo_dir="${1}"
    append_maven_options "-P test"
    update_origin_with_fake_remote "${repo_dir}"
}

function usage {
    echo "Usage: $0 [option...] {release_early|release|release_hotfix|deploy|test|test_cli}"
    echo
    echo "Command line interface to build, package and deploy Jongo"
    echo "Note that by default all tasks are ran in dry mode. Set '--dry-run false' to run it for real. "
    echo
    echo "   -b, --branch               The branch where task will be executed"
    echo "   -t, --tag                  The git tag used to deploy artifacts (only used by deploy task)"
    echo "   -d, --dry-run              Run task in dry mode. Nothing will be pushed nor deployed (default: true)"
    echo "   --early                    Run Maven with the early profile"
    echo "   --gpg-file                 Path the GPG file used to sign artifacts"
    echo "   --settings-file            Path to the Maven settings file (default: ~/.m2/settings.xml)"
    echo "   --settings-security        Path to the Maven security file (default: ~/.m2/settings-security.xml)"
    echo "   --dirty                    Do not clean generated resources during execution (eg. cloned repository)"
    echo "   --debug                    Print all executed commands and run Maven in debug mode"
    echo
    echo "Usage examples:"
    echo ""
    echo "  #Release the master branch"
    echo "  bash ./bin/cli.sh \\"
    echo "      release \\"
    echo "      --settings-file /path/to/settings.xml \\"
    echo "      --settings-security /path/to/settings-security.xml \\"
    echo "      --gpg-file /path/to/file.gpg \\"
    echo "      --branch master"
    echo ""
    echo "  #Deploy a version from inside a docker container"
    echo "  docker build bin -t jongo-docker-image && \\"
    echo "  docker run -it \\"
    echo "      --volume \$SSH_AUTH_SOCK:/ssh-agent \\"
    echo "      --env SSH_AUTH_SOCK=/ssh-agent \\"
    echo "      --volume /path/to/conf:/opt/jongo/conf jongo-docker-image \\"
    echo "       deploy \\"
    echo "      --settings-file /opt/jongo/conf/settings.xml \\"
    echo "      --settings-security /opt/jongo/conf/settings-security.xml \\"
    echo "      --gpg-file /opt/jongo/conf/file.gpg \\"
    echo "      --tag 42.0.0"
}

function __main() {

    local dry_run=true
    local dirty=false
    local debug=false
    local early=false
    local task=()

    while [[ $# -gt 0 ]]
    do
    key="$1"
    case $key in
        -b|--branch)
            local -r target_branch="$2"
            shift
            shift
        ;;
        -t|--tag)
            local -r tag="$2"
            shift
            shift
            ;;
        --early)
            readonly early=true
            shift
        ;;
        --gpg-file)
            log_info "Importing gpg file ${2} into keyring..."
            local -r gpg_keyname=$(import_gpg "${2}")
            append_maven_options "-Dgpg.keyname=${gpg_keyname}"
            append_maven_options "-Dgpg.passphraseServerId=jongo.gpg.passphrase.server"
            shift
            shift
        ;;
        --settings-file)
            append_maven_options "--settings ${2}"
            shift
            shift
        ;;
        --settings-security)
            append_maven_options "-Dsettings.security=${2}"
            shift
            shift
        ;;
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
        -?|--help)
            usage
            exit 0;
        ;;
        *)
        task+=("$1")
        shift
        ;;
    esac
    done
    set -- "${task[@]}"

    local repo_dir=$(clone_repository "https://github.com/bguerout/jongo.git")

    [[ "${dry_run}" = true ]] && configure_dry_mode "${repo_dir}" && log_warn "Script is running in dry mode." || safeguard "${task}"
    [[ "${early}" = true ]] && append_maven_options "-P early"
    [[ "${debug}" = false ]] &&  append_maven_options "--quiet"
    [[ "${dirty}" = false ]] && trap clean_resources EXIT || log_warn "Dirty mode activated."

    pushd "${repo_dir}" > /dev/null

        log_info "***************************************************************************************"
        log_success "* Running ${task}..."
        log_info "*   Repository:     '${repo_dir}'"
        log_info "*   Maven options:  '$(get_maven_options)'"
        log_info "*   Dry mode:       '${dry_run}'"
        log_info "*   Target branch:  '${target_branch:-none}'"
        log_info "*   GPG key:        '${gpg_keyname:-none}'"
        log_info "***************************************************************************************"
        echo ""

        case "${task}" in
            test)
                test_jongo "${target_branch}"
            ;;
            test_cli)
                source "${JONGO_BASE_DIR}/bin/test/test-tasks.sh"
                run_test_suite "${target_branch}"
            ;;
            release_early)
                create_early_release "${target_branch}"
            ;;
            release)
                create_release "${target_branch}"
            ;;
            release_hotfix)
                create_hotfix_release "${target_branch}"
            ;;
            deploy)
                deploy "${tag}"
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