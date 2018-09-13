# ICOnator: KYC Application

## Description

To be described

## API

**kyc/start**
- Starts the KYC process for all investors, provided they have invested and the KYC process
has not already been started. The KYC URL will be generated automatically.
- Method: `POST`

**kyc/\<investorId\>/start**
- Starts the KYC process with the provided investor. The KYC URL will be generated automatically if not provided.
- Method: `POST`
- Data params:
  - Optional: Externally generated KYC URL
    - e.g. `'http://kycprovider.com/user/123456789'`
    
**kyc/\<investorId\>/remind**
- Sends a reminder email with the KYC link to the provided investor.
- Method: `POST`
    
**kyc/fetchall**
- Fetches all completed KYC identifications from the KYC provider and sets their KYC status to complete.
- Method: `POST`

**kyc/\<investorId\>/complete**
- Completes the KYC process with the provided Investor.
- Method: `POST`

**kyc/\<investorId\>/status**
- Returns the current KYC status of the provided Investor.
- Method: `GET`

## Table
Table name: `kyc_info`

| kyc_uuid | investor_id | is_start_kyc_email_sent | no_of_reminders_sent | is_kyc_complete | kyc_uri |
|---|---|---|---|---|---|
|`UUID`|`Long`|`Boolean`|`Integer`|`Boolean`|`String`|