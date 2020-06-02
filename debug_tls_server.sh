#!/bin/bash
openssl s_server -cipher ECDHE-PSK-CHACHA20-POLY1305 -nocert -tls1_2 -psk 1a2b3c4d -debug
