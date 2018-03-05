
function log_error() {
    printf "\e[91m[ERROR] %s\e[39m\n" "$@" >&2
}

function log_warn() {
    printf "\e[93m[WARN] %s\e[39m\n" "$@" >&2
}

function log_info() {
    printf "\e[39m[INFO] %s\e[39m\n" "$@" >&2
}
