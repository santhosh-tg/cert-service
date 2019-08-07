const config = {
    "issuer": {
        "name": "" || process.env.ISSUER_NAME,
        "url": "" || process.env.ISSUER_URL,
        "description": "" || process.env.ISSUER_DESCRIPTION,
        "type": "Issuer",
        "image": "" || process.env.ISSUER_IMAGE,
        "publicKey": [""] || [process.env.PUBLICKEY_URL]
    },
    "publicKey": {
        "type": "CryptographicKey",
        "publicKeyPem": "" || process.env.PUBLIC_KEY
    },
    "badge": {
        "type": "BadgeClass",
        "name": "" || process.env.BADGE_NAME, 
        "description": "" || process.env.BADGE_DESCRIPTION,
        "image": "" || process.env.BADGE_IMAGE,
        "criteria": "" || process.env.BADGE_CRITERIA,
        "batchId":"" || process.env.BATCH_ID
    },
    "domainUrl": "" || process.env.DOMAIN_URL, 
    "contextUrl":"" || process.env.CONTEXT_URL
}

module.exports = config;