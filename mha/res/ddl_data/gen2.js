for(var i=0; i<10000000; i+=20) {
    var ay = [];
    ay.push("insert into test(seq, name) values(" + i + ", 'name" + i + "')");
    for(var j = i + 1; j < i+20; j++) {
        ay.push("insert into test(seq, name) values(" + j + ", 'name" + j + "')");
    }
    
    console.log(ay.join(','), ';');
}
