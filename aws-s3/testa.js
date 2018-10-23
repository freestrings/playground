//
// IAM > Users > Security credentials > Create access key
// ~/.aws/credentials
// [default]
// aws_access_key_id = ...
// aws_secret_access_key = ...
//
const AWS = require('aws-sdk');
const s3 = new AWS.S3();
const bucketName = 'directcs-docker-images-test';

s3.createBucket({ Bucket: bucketName }, (err, _) => {
    if(err) {
        if(err.code === 'BucketAlreadyOwnedByYou') {
            console.log('Ok bucket exist');
        } else {
            console.log('Bucket error', err);
            return;
        }
    }

    const params = {
        Bucket: bucketName, Key: "test.txt", Body: 'testa'
    };

    s3.putObject(params, (err, data) => {
        if(err) {
            console.log('Put error', err);
            return;
        }

        console.log('uploaded');
    })
});