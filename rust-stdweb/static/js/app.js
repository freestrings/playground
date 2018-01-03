function initArray(num) {
    let array = [];
    for (let i = 0; i < num; i++) {
        array.push(((Math.random() * 20000) | 0) - 10000);
    }
    return array;
}

function App(module) {
    // let num = 100000000; 크롬죽는다.
    let num = 10000000;
    let array = initArray(num);
    let result = module.sumArray(array);
    console.log(result);
}