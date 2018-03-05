
function get_gpg_key_id() {
    local gpg_file="${1}"
    local keyid=$(gpg -q "${gpg_file}" | grep -e "uid" -B 1 | head -n 1)

    echo "${keyid}"
}

function import_gpg() {
    local gpg_file="${1}"

    export GPG_TTY=$(tty)
    gpg -q --no-tty --batch --import "${gpg_file}"

    echo $(get_gpg_key_id "${gpg_file}")
}