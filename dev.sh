#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $DIR && docker-compose pull && docker-compose -p capstone -f docker-compose.yml -f docker-compose-dev.yml up -d