#!/usr/bin/env bash

jq -c -r . data.json | tr -d '\r' | tr -d '\n' > data_simplified.json

cat index_0.html data_simplified.json index_2.html > _index.html

open _index.html