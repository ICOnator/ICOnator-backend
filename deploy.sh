#!/bin/bash

#servers to deploy to and jumphost for the tunnel
SERVERS=( "tokenapp1.modum.intern" "tokenapp2.modum.intern" )
JUMP_HOST="jump1.modum.io"

#if only one key is provided, both servers have the same key
PRIV_PROXY=$1
if [ "$#" -gt 1 ]; then
   PRIV_APP=$2
else
   PRIV_APP=$1
fi

if [ ! -f "$PRIV_PROXY" ]; then
    echo "Proxy private key not found! Please make sure you have the valid private key: deploy.sh private_proxy.key private_app.key"
    exit 1
fi

if [ ! -f "$PRIV_APP" ]; then
    echo "App private key not found! Please make sure you have the valid private key: deploy.sh private_proxy.key private_app.key"
    exit 1
fi

# Build the backend
if ! ./gradlew clean services:backend:build; then
    echo "gradle build failed"
    exit 1
fi

# Deployment
# Make sure to have a systemd init script as found in: https://docs.spring.io/spring-boot/docs/current/reference/html/deployment-install.html

# Deploy backend service on both servers
for i in "${SERVERS[@]}"
do
  # Open ssh tunnel (http://www.g-loaded.eu/2006/11/24/auto-closing-ssh-tunnels/)
  echo "Opening SSH tunnel to proxy server..."
  ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -f -L 1234:"$i":22 -i "$PRIV_PROXY" -p 2202 ubuntu@"$JUMP_HOST" sleep 3;
  echo "Uploading jar file"
  scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no  -r -i "$PRIV_APP" -P 1234 services/backend/build/libs/backend-*-boot.jar ubuntu@localhost:/var/lib/backend/backend.jar
  sleep 3

  echo "Opening SSH tunnel to proxy server..."
  ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -f -L 1234:"$i":22 -i "$PRIV_PROXY" -p 2202 ubuntu@"$JUMP_HOST" sleep 3;
  echo "Restarting backend service"
  ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no  -i "$PRIV_APP" -p 1234 ubuntu@localhost sudo systemctl restart backend.service
  sleep 3
done
