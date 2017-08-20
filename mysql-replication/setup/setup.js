const sync = require('child_process').spawnSync;
const mysql = require('mysql');

const MASTER = {
    host: 'localhost',
    user: 'root',
    password: 'root'
};
const SLAVE = {
    host: 'localhost',
    port: '3307',
    user: 'root',
    password: 'root'
};
const SLAVE_OF_SLAVE = {
    host: 'localhost',
    port: '3308',
    user: 'root',
    password: 'root'
};

if (runAndCheck('./master-config') === true && runAndCheck('./slave-config') === true && runAndCheck('./slave-of-slave-config') === true) {
    grantReplication()
        .then(getMasterInfo)
        .then(configureSlave)
        .then(startSlave)
        .then(getSlaveInfo)
        .then(grantToSlaveOfSlaveReplication)
        .then(getMasterInfoOfSlave)
        .then(configureSlaveOfSlave)
        .then(startSlaveOfSlave)
        .then(getSlaveOfSlaveInfo)
        .then(function() { console.log('Done!') })
        .catch(console.error);
} else {
    console.error('Fail to configuration');
}

function runAndCheck(command) {
    var out = sync('bash', [command]).stdout.toString();
    try {
        return JSON.parse(out)[0].State.Running;
    } catch (e) {
        console.error(command, e, '[', out, ']');
        return false;
    }
}

function query(retry, info, sql, callback) {
    if (retry <= 0) {
        callback({ error: true, message: 'Retry fail:' + sql });
        return;
    }
    var connection = mysql.createConnection(info);
    connection.connect();
    connection.query(sql, function(err, results) {
        if (err) {
            process.stdout.write(".");
            setTimeout(function() {
                query(retry - 1, info, sql, callback);
            }, 1000);
        } else {
            process.stdout.write(" Ok\n");
            callback(results);
        }
    });
    connection.end();
}

function grantReplication() {
    process.stdout.write('grantReplication');

    return new Promise(function(resolve, reject) {
        query(10,
            MASTER,
            "GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%' IDENTIFIED BY 'slavepass'",
            function(results) {
                if (results.error) {
                    reject(results.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function getMasterInfo() {
    process.stdout.write('getMasterInfo');

    return new Promise(function(resolve, reject) {
        query(10,
            MASTER,
            'SHOW MASTER STATUS',
            function(results) {
                if (results.error) {
                    reject(results.message);
                } else {
                    resolve(results[0]);
                }
            }
        )
    });
}

function configureSlave(masterInfo) {
    process.stdout.write('configureSlave:' + masterInfo.File + ',' + masterInfo.Position);

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE, [
                'CHANGE MASTER TO MASTER_HOST="mysql-master"',
                'MASTER_USER="repl"',
                'MASTER_PASSWORD="slavepass"',
                'MASTER_LOG_FILE="' + masterInfo.File + '"',
                'MASTER_LOG_POS=' + masterInfo.Position
            ].join(','),
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function startSlave() {
    process.stdout.write('startSlave');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE,
            'START SLAVE',
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function getSlaveInfo() {
    process.stdout.write('getSlaveInfo');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE,
            'SHOW SLAVE STATUS',
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    console.log(results[0]);
                    resolve();
                }
            }
        )
    });
}

function grantToSlaveOfSlaveReplication() {
    process.stdout.write('grantToSlaveOfSlaveReplication');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE,
            "GRANT REPLICATION SLAVE ON *.* TO 'repl'@'%' IDENTIFIED BY 'slavepass'",
            function(results) {
                if (results.error) {
                    reject(results.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function getMasterInfoOfSlave() {
    process.stdout.write('getMasterInfoOfSlave');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE,
            'SHOW MASTER STATUS',
            function(results) {
                if (results.error) {
                    reject(results.message);
                } else {
                    resolve(results[0]);
                }
            }
        )
    });
}

function configureSlaveOfSlave(masterInfo) {
    process.stdout.write('configureSlaveOfSlave:' + masterInfo.File + ',' + masterInfo.Position);

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE_OF_SLAVE, [
                'CHANGE MASTER TO MASTER_HOST="mysql-slave"',
                'MASTER_USER="repl"',
                'MASTER_PASSWORD="slavepass"',
                'MASTER_LOG_FILE="' + masterInfo.File + '"',
                'MASTER_LOG_POS=' + masterInfo.Position
            ].join(','),
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function startSlaveOfSlave() {
    process.stdout.write('startSlaveOfSlave');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE_OF_SLAVE,
            'START SLAVE',
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    resolve();
                }
            }
        )
    });
}

function getSlaveOfSlaveInfo() {
    process.stdout.write('getSlaveOfSlaveInfo');

    return new Promise(function(resolve, reject) {
        query(10,
            SLAVE_OF_SLAVE,
            'SHOW SLAVE STATUS',
            function(results) {
                if (results.error) {
                    reject(result.message);
                } else {
                    console.log(results[0]);
                    resolve();
                }
            }
        )
    });
}