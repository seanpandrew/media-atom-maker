#!/usr/bin/env bash

set -e

npm install
npm test
npm run old-build
npm run build
./sbt-tc clean compile test riffRaffUpload
