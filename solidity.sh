#!/bin/sh

if [ "$#" -ne 1 ]; then
   echo "need to provide the path, where ModumToken.sol can be found"
   exit 1
fi

SOL_DIR=$1

solc "$SOL_DIR"/ModumToken.sol --bin --abi --optimize -o /tmp/
web3j solidity generate /tmp/ModumToken.bin /tmp/ModumToken.abi -o services/minting/src/main/java -p io.modum.tokenapp.minting.service
