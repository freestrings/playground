const spawn = require('child_process').spawn;

var total = 10000;
var worker = 1;
var amount = total / worker;
for(var i = 0 ; i < worker; i++) {
    (function(index) {
        var time = Date.now();
        const update = spawn('java', [
            '-jar',
            'target/springframework-data-elasticsearch-1.0-SNAPSHOT.jar',
            'update-single',
            amount,
            index,
            '2000',
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