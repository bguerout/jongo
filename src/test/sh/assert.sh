#!/usr/bin/env bash

#####################################################################
##
## title: Assert Extension
##
## description:
## Assert extension of shell (bash, ...)
##   with the common assert functions
## Function list based on:
##   http://junit.sourceforge.net/javadoc/org/junit/Assert.html
## Log methods : inspired by
##	- https://natelandau.com/bash-scripting-utilities/
## author: Mark Torok
##
## date: 07. Dec. 2016
##
## license: MIT
##
#####################################################################

RED='\e[0;31m'
GREEN='\e[0;32m'
MAGENTA='\e[1;35m'
NORMAL='\e[39m'

log_header() {
  printf "\n${MAGENTA}==========  %s  ==========${NORMAL}\n" "$@" >&2
  }

log_success() {
  printf "${GREEN}✔ %s${NORMAL}\n" "$@" >&2
}

log_failure() {
  printf "${RED}✖ %s${NORMAL}\n" "$@" >&2
}


assert_eq() {
  local expected="$1"
  local actual="$2"
  local msg

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  if [ "$expected" == "$actual" ]; then
    return 0
  else
    [ "${#msg}" -gt 0 ] && log_failure "$expected == $actual :: $msg" || true
    return 1
  fi
}

assert_not_eq() {
  local expected="$1"
  local actual="$2"
  local msg

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  if [ ! "$expected" == "$actual" ]; then
    return 0
  else
    [ "${#msg}" -gt 0 ] && log_failure "$expected != $actual :: $msg" || true
    return 1
  fi
}

assert_true() {
  local actual
  local msg

  actual="$1"

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  assert_eq true "$actual" "$msg"
  return "$?"
}

assert_false() {
  local actual
  local msg

  actual="$1"

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  assert_eq false "$actual" "$msg"
  return "$?"
}

assert_array_eq() {

  declare -a expected=("${!1}")
  # echo "AAE ${expected[@]}"

  declare -a actual=("${!2}")
  # echo "AAE ${actual[@]}"

  local msg
  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  local return_code
  return_code=0
  if [ ! "${#expected[@]}" == "${#actual[@]}" ]; then
    return_code=1
  fi

  local i
  for (( i=1; i < ${#expected[@]} + 1; i+=1 )); do
    if [ ! "${expected[$i-1]}" == "${actual[$i-1]}" ]; then
      return_code=1
      break
    fi
  done

  if [ "$return_code" == 1 ]; then
    [ "${#msg}" -gt 0 ] && log_failure "(${expected[*]}) != (${actual[*]}) :: $msg" || true
  fi

  return "$return_code"
}

assert_array_not_eq() {

  declare -a expected=("${!1}")
  declare -a actual=("${!2}")

  local msg
  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  local return_code
  return_code=1
  if [ ! "${#expected[@]}" == "${#actual[@]}" ]; then
    return_code=0
  fi

  local i
  for (( i=1; i < ${#expected[@]} + 1; i+=1 )); do
    if [ ! "${expected[$i-1]}" == "${actual[$i-1]}" ]; then
      return_code=0
      break
    fi
  done

  if [ "$return_code" == 1 ]; then
    [ "${#msg}" -gt 0 ] && log_failure "(${expected[*]}) == (${actual[*]}) :: $msg" || true
  fi

  return "$return_code"
}

assert_empty() {
  local actual
  local msg

  actual="$1"

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  assert_eq "" "$actual" "$msg"
  return "$?"
}

assert_not_empty() {
  local actual
  local msg

  actual="$1"

  if [ "$#" -ge 3 ]; then
    msg="$3"
  fi

  assert_not_eq "" "$actual" "$msg"
  return "$?"
}