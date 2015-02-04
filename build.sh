#!/bin/sh
set -e

VERSION="$1"

if [ "$VERSION" == '' ]; then
	exit 1
fi

echo Building $VERSION ...

sbt 'set scalaVersion := "2.11.4"' "set version := \"${VERSION}\"" publish
