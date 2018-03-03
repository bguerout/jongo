
function import_gpg() {
    local gpg_file="${1}"

    log_info "Importing gpg file ${gpg_file} into keyring..."
    export GPG_TTY=$(tty)
    gpg -q --no-tty --batch --import "${gpg_file}"
}