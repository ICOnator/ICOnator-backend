#!/bin/bash

solc /home/draft/go/src/github.com/modum-io/smartcontract/ModumToken.sol --bin --abi --optimize -o /tmp/
web3j solidity generate /tmp/ModumToken.bin /tmp/ModumToken.abi -o /home/draft/git/tokenapp-backend/services/backend/src/main/java -p io.modum.tokenapp.backend.service
