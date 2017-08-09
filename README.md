![modum.io Logo](https://assets.modum.io/wp-content/uploads/2017/03/modum_logo_white_space-cropped.png)

# modum.io TokenApp Backend

## Description

This is the code for the modum token sale app backend, found under https://token.modum.io. For more information, visit https://modum.io/tokensale

## API

### Register/Send Email

```POST /register```

Payload: application/json
```json 
{
  "email": "investor@something.com"
}
```

Server sends email with link to 
`/frontend/wallet/{generated confirmation token}`
and returns 201 CREATED.

### Confirmation of Email

``` GET /register/{confirmation token}```

Server sets confirmed to true and returns 200 OK

### Address validation

Ethereum address:

```GET /address/eth/{address}/validate```

Bitcoin address:

```GET /address/btc/{address}/validate```

Server returns 200 OK if it's a valid address.
Otherwise, it returns 5xx.

### Save the address

The `address` refers to the user's Ethereum wallet.

``` POST /address ```

Headers:
`Authorization: Bearer <token>`

Payload: application/json
```json
{
  "address": "0xcd2a3d9f938e13cd947ec05abc7fe734df8dd82", // Ether address
  "refundBTC": "1BoatSLRHtKNngkdXEeobR76b53LETtpyT", // Refund bitcoin address
  "refundETH": "0xcd2a3d9f938e13cd947ec05abc7fe734df8dd82" // Refund ethereum address
}
```

Returns: application/json

```json
{
  "eth": "0xcd6f39a8b....", // Pay-in address for ether
  "btc": "1BoatSLRHtKNngkdXEeobR76b53LETtpyT" // Pay-in address for btc
}
```
