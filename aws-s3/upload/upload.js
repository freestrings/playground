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
const base64Data = new Buffer.from(require('fs').readFileSync(file), "Binary");
s3.upload({
    Bucket: bucket, Key: file, Body: base64Data
}, (err, data) => {
    if(err) console.error('Error', err);
    console.log('Done', data);
})