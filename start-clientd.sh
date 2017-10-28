#!/bin/bash

java -jar target/great-free-will-1.0.0.jar \
    --daemon.mode=clientd \
    --daemon.secret=afdlljldf23423## \
    --client.daemon.port=8080 \
    --server.daemon.port=9090 \
    --server.daemon.host=127.0.0.1 \
    1>/dev/null 2>&1 &

