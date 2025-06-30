# Certificate renewal

The client certificates used for mTLS have an expiry date and
clients must be issued a new certificate before their old ones
expire.

Issuing replacement certificates is a similar process to setting 
up a new consumer in most respects.

## Certificate expiry notifications

Notifications of pending certificate expiry are sent to the
`#hmpps-integration-api-alerts` Slack channel every day, starting 30 days prior to expiry.

`The certificate for {name} in {environment} will expire within the next {duration} days`

When a new certificate is created and uploaded to the backup S3
bucket the expiry notifications will cease.

## Certificate renewal process

### 1. Find contact details

For internal users and dev environments, try to find a Slack user
matching the certificate backup file name. 

For external clients, use the 
[client list in Confluence](https://dsdmoj.atlassian.net/wiki/spaces/HIA/pages/5544181873/Clients).
If the client has a page there with a technical contact email 
address then use that. If there is a business contact but no
technical contact, reach out to the business contact to find a
technical contact.

### 2. Contact the client

For external clients, email the contact to notify them that their
certificate will be expiring soon, and asking them to generate a
new key pair and send the public key.

> Hello
>
> I'm on the team who are currently maintaining the HMPPS Integration API.
> 
> Your certificates are due to expire for the {environment} environment of the HMPPS Integration
> API in {duration} days, at which point you won't be able to access it.
> So that I can send you new credentials, please can you complete the following ASAP.
> Please generate keys following the steps below, and then send me only the public key:
>
> **Generate private key**
> 
>`openssl genrsa -out hmpps-integration-api-cred-exchange-private-key.pem 3072`
>
> **Generate public key**
> 
>`openssl rsa -in hmpps-integration-api-cred-exchange-private-key.pem -pubout -out hmpps-integration-api-cred-exchange-public-key.pem`
>
>These keys are used to encrypt the keys that we generate on our side so they can be sent to you safely. Please keep the public and private keys that you generate in the above steps on your side until I have sent you the new keys.
>
>If you are unable to generate these keys, please let me know and we can find an alternative way of sending the credentials.
>
>Thanks
> 
> {your name}
> 

With internal dev clients we can just have a conversation on Slack.

### 3. Generate the new certificate and send to the client

Once the encryption public key is received from the client, follow 
the same process as for [setting up a new consumer](https://github.com/ministryofjustice/hmpps-integration-api/blob/main/docs/guides/setting-up-a-new-consumer.md#setting-up-a-new-consumer),
but skipping the "Create an API key" and "Configure allowed endpoints for the consumer" sections.

The previous API key has no expiry.

Note that the current certificate will not be invalidated when a new certificate is generated.


