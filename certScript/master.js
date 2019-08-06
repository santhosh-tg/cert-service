const config = require("./config.js");
var fs = require('fs');

var badge = {
    "id": config.badge.id,
    "type": config.badge.type,
    "@context": config.badge.context,
    "name": config.badge.name,
    "description": config.badge.description,
    "image": config.badge.image,
    "criteria": config.badge.criteria,
    "issuer": config.badge.id
}
var issuer = {
    "context": config.issuer.context,
    "type": config.issuer.type,
    "id": config.issuer.id,
    "name": config.issuer.name,
    "description": config.issuer.description,
    "url": config.issuer.url,
    "image": config.issuer.image,
    "publicKey": config.issuer.publicKey
}


var publicKey = {
    "context": config.publicKey.context,
    "type": config.publicKey.type,
    "id": "" || config.publicKey.id,
    "owner": "" || config.publicKey.owner,
    "publicKeyPem": "" || config.publicKey.publicKeyPem
}


console.log(JSON.stringify(publicKey))


var methods = {
    createPublicKeyJson : function () {
        if (writeToFile("./target/publicKey.json", publicKey)) {
            console.log("publicKey json is created")
        }
    },
    createIssuerJson : function () {
        if (writeToFile("./target/issuer.json", issuer)) {
            console.log("issuer json is created")
        }
    },
    createBadgeJson : function () {
        if (writeToFile("./target/badge.json", badge)) {
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