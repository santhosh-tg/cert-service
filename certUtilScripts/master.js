const config = require("./config.js");
var fs = require('fs');

var badge = {
    "id":  config.domainUrl + config.badge.batchId + "/badge.json",
    "type": "BadgeClass",
    "@context": config.contextUrl,
    "name": config.badge.name,
    "description": config.badge.description,
    "image": config.badge.image,
    "criteria": config.badge.criteria,
    "issuer": config.domainUrl + "/issuer.json"
}
var issuer = {
    "@context": config.contextUrl,
    "type": "Issuer",
    "id": config.domainUrl + "/issuer.json",
    "name": config.issuer.name,
    "description": config.issuer.description,
    "url": config.issuer.url, 
    "image": config.issuer.image,
    "publicKey": config.issuer.publicKey
}


var publicKey = {
    "@context": config.contextUrl,
    "type": "CryptographicKey",
    "id": config.domainUrl + "/publickey.json",
    "owner": config.domainUrl+ "/issuer.json",
    "publicKeyPem": config.publicKey.publicKeyPem
}

var outDirName = "./out";

var methods = {
    createPublicKeyJson: function () {
        if (writeToFile(outDirName + "/publicKey.json", publicKey)) {
            console.log("publicKey json is created")
        }
    },
    createIssuerJson: function () {
        if (writeToFile(outDirName + "/issuer.json", issuer)) {
            console.log("issuer json is created")
        }
    },
    createBadgeJson: function () {
        if (!fs.existsSync(outDirName + "/" + config.badge.batchId)) {
            fs.mkdirSync(outDirName + "/" + config.badge.batchId);
        }
        if (writeToFile(outDirName + "/" + config.badge.batchId + "/badge.json", badge)) {
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