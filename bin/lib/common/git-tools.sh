
function checkout() {
   git checkout -q $@
}

function uncheckout() {
   git checkout -q -
}

function git_commit() {
    git -c user.name="Jongo Release" -c user.email="release@jongo.org" commit -q -a -m "${1}"
}

function create_bare_repository {
    local source_repo=${1}
    local bare_repo_dir=$(mktemp -d -t "jongo-release-bare-repo-XXXXX")
    cp -R ${source_repo}/.git/ ${bare_repo_dir}/
    pushd ${bare_repo_dir} > /dev/null
        git config --bool core.bare true
        git fetch -q origin '*:*'
    popd > /dev/null
    echo "${bare_repo_dir}"
}

function clone_repository {
    local remote_url="${1}"
    local clone_dir=$(mktemp -d -t "jongo-release-repo-XXXXX")
    git clone -q "${remote_url}" "${clone_dir}"
    echo "${clone_dir}"
}

function update_origin_with_fake_remote {
    local repo_dir="${1}"
    local bare_repo_dir=$(create_bare_repository ${repo_dir})

    pushd ${repo_dir} > /dev/null
        git remote remove origin
        git remote add origin "${bare_repo_dir}"
        git fetch -q origin
    popd > /dev/null
}

function clean_resources {
    log_info "Cleaning resources..."
    find ${TMPDIR:-$(dirname $(mktemp))/} -depth -type d -name "jongo-release*" -exec rm -rf {} \;
}
