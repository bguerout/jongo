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
        read -p "[WARN] Do you really want to run '${task}' for real?" yn
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
    echo "Usage: $0 [option...] {release|release_hotfix|test}"
    echo
    echo "Command line interface to build, package and deploy Jongo"
    echo "Note that by default all tasks are ran in dry mode. Set '--dry-run false' to run it for real. "
    echo
    echo "   -b, --branch               The branch where task will be executed (default: master)"
    echo "   -t, --tag                  The tag used to deploy artifacts (required for deploy task)"
    echo "   -d, --dry-run              Run task in dry mode. Nothing will be pushed nor deployed (default: true)"
    echo "   --early                    Run release in early mode"
    echo "   --gpg-file                 Path to the GPG file used to sign artifacts"
    echo "   --maven-options            Maven options (eg. --settings /path/to/settings.xml)"
    echo "   --dirty                    Do not clean generated resources during execution (eg. cloned repository)"
    echo "   --debug                    Print all executed commands and run Maven in debug mode"
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

function __main() {

    local git_revision='master'
    local dry_run=true
    local dirty=false
    local debug=false
    local early=false
    local positional=()

    while [[ $# -gt 0 ]]
    do
    key="$1"
    case $key in
        -b|-t|--branch|--tag)
            local git_revision="$2"
            shift
            shift
        ;;
        --early)
            readonly early=true
            shift
        ;;
        --gpg-file)
            local -r gpg_keyname=$(import_gpg "${2}")
            log_info "GPG key ${gpg_keyname} imported from file ${2}"
            append_maven_options "-Dgpg.keyname=${gpg_keyname}"
            append_maven_options "-Dgpg.passphraseServerId=jongo.gpg.passphrase.server"
            shift
            shift
        ;;
        --maven-options)
            append_maven_options "${2}"
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
        positional+=("$1")
        shift
        ;;
    esac
    done
    set -- "${positional[@]}"

    local task="${1}"
    [[ "${early}" = true ]] && append_maven_options "-P early"
    [[ "${debug}" = false ]] &&  append_maven_options "--quiet"
    [[ "${dirty}" = false ]] && trap clean_resources EXIT || log_warn "Dirty mode activated."
    [[ "${dry_run}" = true ]] && log_warn "Script is running in dry mode."

    log_info "Cloning repository..."
    local repo_dir=$(clone_repository "https://github.com/bguerout/jongo.git")
    [[ "${dry_run}" = true ]] && configure_dry_mode "${repo_dir}" || safeguard "${task}"

    echo ""
    log_info    "***************************************************************************************"
    log_success "* Running task '${task}'..."
    log_info    "*   Maven options:  '$(get_maven_options)'"
    log_info    "*   Git revision:   '${git_revision}'"
    log_info    "***************************************************************************************"
    echo ""

    pushd "${repo_dir}" > /dev/null

        download_all_dependencies "${git_revision}"

        case "${task}" in
            test)
                source "${JONGO_BASE_DIR}/bin/test/test-tasks.sh"
                test_jongo "${git_revision}"
                test_cli "${git_revision}"
            ;;
            release)
                if [ "${early}" = true ]; then
                    create_early_release "${git_revision}"
                else
                    create_release "${git_revision}"
                fi
                deploy "${git_revision}"
            ;;
            release_hotfix)
                create_hotfix_release "${git_revision}"
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