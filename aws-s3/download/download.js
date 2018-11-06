const AWS = require('aws-sdk');

let accessKeyId = process.argv[2];
let secretAccessKey = process.argv[3];
let bucket = process.argv[4];
let file = process.argv[5];

AWS.config.update({
    accessKeyId: accessKeyId,
    secretAccessKey: secretAccessKey
});

const s3 = new AWS.S3();
let fileStream = require('fs').createWriteStream(`/downloaded/${file}`);
s3.getObject({ Bucket: bucket, Key: file }).createReadStream().pipe(fileStream);