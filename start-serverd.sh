#!/bin/bash

java -jar target/great-free-will-1.0.0.jar \
    --daemon.mode=serverd \
    --daemon.secret=afdlljldf23423## \
    --server.daemon.port=9090 \
    1>/dev/null 2>&1 &

