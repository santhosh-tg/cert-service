const config = require("./config.js");
var fs = require('fs');

var badge = {
    "id":  config.domainUrl + config.badge.batchId + "/badge.json",
    "type": "BadgeClass",
    "@context": process.env.PROTO + process.env.DOMAIN_URL + "/" + process.env.CONTAINER_NAME + "container/v1context.json",
    "name": config.badge.name,
    "description": config.badge.description,
    "image": config.badge.image,
    "criteria": config.badge.criteria,
    "issuer": config.domainUrl + process.env.ROOT_ORG_ID + "/issuer.json"
}
var issuer = {
    "@context": config.contextUrl,
    "type": "Issuer",
    "id": config.domainUrl + process.env.ROOT_ORG_ID + "/issuer.json",
    "name": config.issuer.name,
    "email": config.issuer.email,
    "url": config.issuer.url, 
    "image": config.issuer.image,
    "publicKey": config.issuer.publicKey
}


var publicKey = {
    "@context": config.contextUrl,
    "type": "CryptographicKey",
    "id": config.domainUrl + "/" + process.env.ROOT_ORG_ID + "_publickey.json",
    "owner": config.domainUrl + "/" + process.env.ROOT_ORG_ID + "_issuer.json",
    "publicKeyPem": ""
}

var outDirName = "./out";

var methods = {
    createPublicKeyJson: function () {
        if (writeToFile(outDirName + "/" + process.env.ROOT_ORG_ID + "_publicKey.json", publicKey)) {
            console.log("publicKey json is created")
        }
    },
    createIssuerJson: function () {
        let issuerDir = outDirName + "/" + process.env.ROOT_ORG_ID
        if (!fs.existsSync(issuerDir)) {
            fs.mkdirSync(issuerDir);
        }
        if (writeToFile(issuerDir + "/" + process.env.ROOT_ORG_ID + "_issuer.json", issuer)) {
            console.log("issuer json is created")
        }
    },
    createBadgeJson: function () {
        let batchDir = outDirName + "/" + process.env.ROOT_ORG_ID + "/" + config.badge.batchId
        if (!fs.existsSync(batchDir)) {
            if (!fs.existsSync(outDirName + "/" + process.env.ROOT_ORG_ID)) {
                fs.mkdirSync(outDirName + "/" + process.env.ROOT_ORG_ID);
            }
            fs.mkdirSync(batchDir);
        }
        if (writeToFile(batchDir + "/badge.json", badge)) {
            console.log("Badge json is created")
        }
    }
}

const writeToFile = (fileName, keys) => {
    fs.writeFile(fileName, JSON.stringify(keys), function (err) {
        if (err) {
            return console.log(err);
        }
    });
    return true;
}

module.exports = methods