# 자바에서 메모리를 제한하는 방법과 쓰레드 처리

## ByteBuffer.allocate vs ByteBuffer.allocateDirect

생명주기가 짧거나 자주 사용되지 않는 객체에는 다이렉트 버퍼를 사용하지 않아야 한다. 왜냐하면, 
다이렉트 버퍼는 OS 종속적인 네이티브 코드를 사용하기 때문에 힙기반 버퍼보다 생성과 메모리 반환 비용이 높고 가비지 컬렉터의 영역 밖이라 메모리 누수가 있을 수 있다. 
용량이 큰 다이렉트 버퍼를 빈번하게 할당하면 OutofMemorryError가 생길 수 있다.

## 스택과 힙

### 스택
```
void foo() {
	int y = 5;
	int z = 100;
}

void bar() {
	int x = 1;

	foo();
}
```

- 변수 3개. ( foo: y, z, bar: x )
- bar 호출

| address | name | value |
|---| ---| ---|
|0|x|1|

- foo() 호출

|address | name | value|
|---| ---| ---|
|2|z|100|
|1|y|5|
|0|x|1|

- foo() 호출이 끝나면

|address | name | value|
|---| ---| ---|
|0|x|1|

### 힙

```
void main() {
    MyObj m = new MyObj();
    int y = 42;
}
```

|address | name | value|
|---| ---| ---|
|1|y|42|
|0|m|???|


|address | name | value|
|---| ---| ---|
|???|-| MyObj|
|1|y|42|
|0|m|???|


## non-direct buffer vs direct buffer 속도
버퍼가 256KB 보다 작을땐 non-direct Buffer가 훨씬 빠르고, 256KB 보다 클땐 direct Buffer가 약간 빠르다


## 쓰레드 처리

### ReentrantLock과 Condition

- [기본적인 쓰레딩](src/main/java/fs/ThreadDefault.java)
- [쓰레드 종료대기](src/main/resources/pthread1.c)
- [쓰레드 대기와 깨움1](src/main/java/fs/ShareBasic.java)
- [쓰레드 대기와 깨움2](src/main/java/fs/Share.java)
- [카프카 BufferPool](src/main/java/fs/BufferPool.java)


# 원글
[카프카 프로듀서의 버퍼풀](http://free-strings.blogspot.kr/2016/06/producer-bufferpool.html)
[예제코드](https://gist.github.com/freestrings/f252af60cb7a992ee2df0dfd7c39cfa0)