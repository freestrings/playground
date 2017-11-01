var http = require('http');

var provider = dealProvider([1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);

workAll();

function dealProvider(ids) {
    return {
        get: function () {
            return [ids.pop(), ids.length];
        }
    }
}

function workAll() {
    var workers = 2;
    var counter = workerCounter(workers);
    for(var i = 0 ; i < workers ; i++) {
        worker().then(console.log).then(function() { workNext(counter); }).catch(checkError)
    }
}

function workNext(counter) {
    if(!counter.touchDown()) {
        workAll();
    }
}

function workerCounter(count) {
    return {
        touchDown: function() {
            count--;
            if(count <= 0) return false;
            else return true;
        }
    }
}

function checkError(err) {
    if(err === null) {
        // do nothing
    } else {
        console.log('error', err);
    }
}

function worker() {
    return new Promise(work(provider.get()));
}

function work(dealCtx) {
    var id = dealCtx[0];
    var remainDealCount = dealCtx[1];

    return function(resolve, reject) {
        if(!id) {
            reject(null);
            return;
        }

        var url = [
            'http://localhost:8080/',
            'ids/',
            id
        ];

        http.get(url.join(''), function (resp) {
            var data = '';

            resp.on('data', function (chunk) {
                data += chunk;
            });

            resp.on('end', function () {
                var msg = [
                    new String(id).padEnd(10),
                    ' [' + data + ']',
                    ' - ' + remainDealCount
                ];
                resolve(msg.join(''));
            });

        }).on('error', function(e) {
            reject(e);
        });
    }
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/padEnd
if (!String.prototype.padEnd) {
    String.prototype.padEnd = function padEnd(targetLength,padString) {
        targetLength = targetLength>>0; //floor if number or convert non-number to 0;
        padString = String(padString || ' ');
        if (this.length > targetLength) {
            return String(this);
        }
        else {
            targetLength = targetLength-this.length;
            if (targetLength > padString.length) {
                padString += padString.repeat(targetLength/padString.length); //append to original to ensure we are longer than needed
            }
            return String(this) + padString.slice(0,targetLength);
        }
    };
}