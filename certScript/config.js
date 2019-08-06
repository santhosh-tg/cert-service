const config = {
    "issuer": {
        "name": "" || process.env.ISSUER_NAME,
        "url": "" || process.env.ISSUER_URL,
        "description": "" || process.env.ISSUER_DESCRIPTION,
        "context": "" || process.env.CONTEXT,
        "type": "Issuer",
        "id": "" || process.env.ISSUER_ID,
        "image": "" || process.env.ISSUER_IMAGE,
        "publicKey": [""] || [process.env.PUBLICKEY_URL]
    },
    "publicKey": {
        "context": "" || process.env.CONTEXT,
        "type": "CryptographicKey",
        "id": "" || process.env.PUBLICKEY_URL,
        "owner": "" || process.env.ISSUER_URL,
        "publicKeyPem": "" || process.env.PUBLIC_KEY
    },
    "badge": {
        "context": "" || process.env.CONTEXT,
        "type": "BadgeClass",
        "id": "" || process.env.BADGE_URL,
        "name": "" || process.env.BADGE_NAME, 
        "description": "" || process.env.BADGE_DESCRIPTION,
        "image": "" || process.env.BADGE_IMAGE,
        "criteria": "" || process.env.BADGE_CRITERIA,
        "issuer": "" || process.env.ISSUER_URL
    }
}

module.exports = config;