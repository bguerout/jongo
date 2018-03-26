
function log_error() {
    printf "\e[91m[ERROR] %s\e[39m\n" "$@" >&2
}

function log_warn() {
    printf "\e[93m[WARN] %s\e[39m\n" "$@" >&2
}

function log_info() {
    printf "\e[94m[INFO] %s\e[39m\n" "$@" >&2
}

function log_task() {
    log_info ""
    log_info "***************************************************************************************"
    log_info "* $@"
    log_info "***************************************************************************************"
    log_info "Maven options: '$(get_maven_options)'"
}

function log_success() {
    printf "\e[32m[SUCCESS] %s\e[39m\n" "$@" >&2
}
