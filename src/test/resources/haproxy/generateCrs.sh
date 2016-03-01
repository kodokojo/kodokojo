#!/bin/sh
openssl req -new -key server.key -out $1-server.csr
openssl x509 -req -days 365 -in $1-server.csr -signkey server.key -out $1-server.crt
cat $1-server.crt server.key | tee $1-server.pem