# Run

```bash
./bench_run.sh 1000000 
```

또는 

```bash
./bench_run.sh 1000000 [java|rust]
```

# 결과

### 속도
* Rust가 1.5 ~ 1.6배 빠름

| 반복 | Java (ms) | Rust (ms) |
| :------------- | :----------: | -----------: |  
| 1,000,000 | 2,498 | 1,615 | 
| 10,000,000 | 23,453 | 15,630 |