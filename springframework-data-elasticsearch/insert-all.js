const spawn = require('child_process').spawn;

var total = 12000;
var worker = 4;
var amount = total / worker;
for(var i = 0 ; i < worker; i++) {
    (function(index) {
        console.log("insert", amount, index, 1000, '-');
        var time = Date.now();
        const update = spawn('java', [
            '-jar',
            'target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar',
            'insert',
            amount,
            i,
            '1000',
            '-'
        ]);

        update.stdout.on('data', (data) => {
            process.stdout.write(data.toString());
        });

        update.stderr.on('data', (data) => {
            console.err(data.toString());
        });

        update.on('close', (code) => {
            var elapsed = Date.now() - time;
            console.log(`${index} exited => ${code} : ${elapsed}`);
        });
    })(i)
}