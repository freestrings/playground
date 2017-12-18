const commandLineArgs = require('command-line-args')
const commandLineOptions = commandLineArgs([
    {name: 'file', alias: 'f', type: String},
    {name: 'version', alias: 'v', type: String},
    {name: 'user', alias: 'u', type: String},
    {name: 'password', alias: 'p', type: String},
    {name: 'space', alias: 's', type: String},
    {name: 'id', alias: 'i', type: String},
]);

function _exit(msg) {
    console.error(msg);
    process.exit(1);
}

if (!commandLineOptions.file) {
    _exit('파일 이름은 필수. 빌드 디렉토리는 자동지정 되기 때문에 파일명만 표기. ex) node doc-uploader.js --file main.pdf ...');
}

if (!commandLineOptions.version) {
    _exit('API 버전은 필수. ex) node doc-uploader.js --version 0.0.1 ...');
}

if (!commandLineOptions.user) {
    _exit('컨플런스 로그인 ID. ex) node doc-uploader.js --user freestrings ...');
}

if (!commandLineOptions.password) {
    _exit('컨플런스 패스워드. ex) node doc-uploader.js --password password ...');
}

if (!commandLineOptions.space) {
    _exit('컨플런스 스페이스 ID. ex) node doc-uploader.js --space ~freestrings ...');
}

if (!commandLineOptions.id) {
    _exit('컨플런스 리소스 ID. ex) node doc-uploader.js --id 11829442 ...');
}

const Confluence = require('confluence-api');

const options = {
    username: commandLineOptions.user,
    password: commandLineOptions.password,
    baseUrl: 'http://confluence.wemakeprice.com'
};

var confluence = new Confluence(options);

function deleteFiles(fileName) {
    return new Promise(function (resolve, reject) {
        fileInfo(fileName)
            .then(function (fileInfos) {
                Promise.all(fileInfos.map(function (fileInfo) {
                    return fileInfo._links.self;
                }).map(deleteFile), resolve());
            })
            .catch(reject);
    });
}

function deleteFile(url) {
    const request = require('superagent');

    return new Promise(function (resolve, reject) {
        request.del(url)
            .auth(options.username, options.password)
            .end(function (err, response) {
                if (err) {
                    reject(err);
                    return;
                }

                resolve(response);
            })
    });
}

function fileInfo(fileName) {
    return new Promise(function (resolve, reject) {
        confluence.getAttachments(commandLineOptions.space, commandLineOptions.id, function (err, data) {
            if (err) {
                reject(err);
                return;
            }

            var attached = data.results.filter(function (result) {
                return result.title == fileName
            });

            resolve(attached);
        });
    });
}

function attachFile(filePath) {

    function _update(fileId, resolve, reject) {
        return function () {
            confluence.updateAttachmentData(commandLineOptions.space, commandLineOptions.id, fileId, filePath, function (err, _) {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(filePath);
            })
        };
    }

    function _create(resolve, reject) {
        confluence.createAttachment(commandLineOptions.space, commandLineOptions.id, filePath, function (err, _) {
            if (err) {
                reject(err);
                return;
            }
            resolve(filePath);
        });
    }

    return new Promise(function (resolve, reject) {
        const path = require('path');
        const fileName = path.basename(filePath);

        fileInfo(fileName).then(function (fileInfos) {
            if (fileInfos.length == 1) {
                _update(fileInfos[0].id, resolve, reject).call();
            } else if (fileInfos.length > 1) {
                //
                // not yet tested;;;
                //
                console.log('동일한 파일 이름이 존재합니다. 중복파일 삭제.');
                deleteFiles(fileInfos.slice(1, fileInfos.length)).then(_update(fileInfos[0].id, resolve, reject)).catch(reject);
            } else {
                _create(resolve, reject);
            }
        });
    });
}

function copyFile(filePath) {
    const fs = require('fs');
    const path = require('path');
    const fileName = path.basename(filePath);
    const extName = path.extname(filePath);
    const dirName = path.dirname(filePath);
    const newFilePath = dirName +
        '/' +
        fileName.replace(extName, '') +
        '-' +
        commandLineOptions.version + extName;

    fs.createReadStream(filePath).pipe(fs.createWriteStream(newFilePath));
    return newFilePath;
}

attachFile(copyFile(commandLineOptions.file))
    .then(function (filePath) {
        console.log('업로드 완료', filePath);
    })
    .catch(function (err) {
        _exit('에러!' + err.toString());
    });