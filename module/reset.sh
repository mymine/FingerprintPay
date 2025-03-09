#!/bin/bash
set -e
cd ${0%/*}
cd ../3rdparty/MagiskModuleTemplate
git reset --hard HEAD
git clean -df .