#!/bin/bash

echo ":codecov: Reporting coverage...";
set +x;
bash <(curl -s https://codecov.io/bash);
