#!/bin/sh
set -x
ant debug && adb install -r bin/TeamUSA-debug.apk
