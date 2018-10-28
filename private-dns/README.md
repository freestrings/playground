# 참고) Openfire Docker 빌드하기

1. 다운로드 https://github.com/igniterealtime/Openfire.git
2. 브랜치 선택
```bash
git branch -a
master
remotes/origin/3.10
remotes/origin/3.9
remotes/origin/4.0
remotes/origin/4.1
remotes/origin/4.2
remotes/origin/HEAD -> origin/master
...

git checkout -b 4.2 origin/4.2
```
3. docker build -t openfire:4.2 .

4. 이미지 저장 

>> docker save -o openfire-4-2.tar openfire:4.2