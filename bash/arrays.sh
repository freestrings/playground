
# ls -al | tail +2 | bash arrays.sh

while read -a LSOUT
do
    DOM=${LSOUT[6]}
    #echo "@:$DOM"
    let CNT[DOM]++
done

#drwxr-xr-x  26 freestrings  staff    884 Jul  8 14:47 .
#drwxr-xr-x  21 freestrings  staff    714 Jun 29 18:34 ..
#-rw-------   1 freestrings  staff  12288 Jul  8 14:47 .arrays.sh.swp
#-rw-r--r--   1 freestrings  staff  12288 Jul  6 11:32 .seteo2.sh.swp
#-rw-r--r--   1 freestrings  staff    139 Jul  5 12:55 args.sh
#-rw-r--r--   1 freestrings  staff    320 Jul  8 14:47 arrays.sh
#-rw-r--r--   1 freestrings  staff     59 Jun 29 17:27 awhile
#-rw-r--r--   1 freestrings  staff     53 Jun 29 17:30 awhile2
#-rw-r--r--   1 freestrings  staff    307 Jun 29 10:40 change
#-rwxr-xr-x   1 freestrings  staff    736 Jun 20 16:17 env.sh
#-rw-r--r--   1 freestrings  staff    226 Jul  4 16:29 fn
#-rw-r--r--   1 freestrings  staff    472 Jul  5 12:32 fn2
#-rw-r--r--   1 freestrings  staff    515 Jul  4 16:52 fnmax
#-rw-r--r--   1 freestrings  staff    159 Jun 28 10:21 foreach
#-rwxr-xr-x   1 freestrings  staff     80 Jun 29 17:37 readls
#-rwxr-xr-x   1 freestrings  staff     36 Jun 27 10:52 rename
#-rw-r--r--   1 freestrings  staff     75 Jul  1 12:54 rpn1
#-rw-r--r--   1 freestrings  staff    173 Jul  4 16:13 rpn2
#-rw-r--r--   1 freestrings  staff    112 Jul  1 12:58 rpn3
#-rw-r--r--   1 freestrings  staff    409 Jul  5 12:59 sayit
#-rw-r--r--   1 freestrings  staff    112 Jul  6 11:31 seteo.sh
#-rw-r--r--   1 freestrings  staff     25 Jul  6 11:32 seteo2.sh
#-rwxr-xr-x   1 freestrings  staff     10 Jun 24 14:27 showit
#-rw-r--r--   1 freestrings  staff    385 Jun 30 12:18 sumls
#-rw-r--r--   1 freestrings  staff    460 Jun 29 17:46 yn

echo "CNT length:"${#CNT[@]}
echo "CNT all value:"${CNT[@]}

#CNT length:11
#CNT all value:2 3 3 3 4 1 1 1 1 6 1

for ((i=0; i<32; i++))
{
    #echo $i ${CNT[i]}
    printf "%2d %3d\n" $i ${CNT[i]}
}

# 0   0
# 1   2
# 2   0
# 3   0
# 4   3
# 5   3
# 6   3
# 7   0
# 8   4
# 9   0
#10   0
#11   0
#12   0
#13   0
#14   0
#15   0
#16   0
#17   0
#18   0
#19   0
#20   1
#21   0
#22   0
#23   0
#24   1
#25   0
#26   0
#27   1
#28   1
#29   6
#30   1
#31   0
echo "------------------------------"

for NM in "${CNT[@]}"
{
    printf "%3d\n" $NM
}


# 0은 할당되지 않은 값을 말한다. ${!CNT[@]} 를 사용하면 0인값을 포함해 인덱스 증가된 값을 얻을 수 있다. 

for NDX in "${!CNT[@]}"
{
    printf "%2d %3d\n" $NDX ${CNT[NDX]}
}

# 1   2
# 4   3
# 5   3
# 6   3
# 8   3
#20   1
#24   1
#27   1
#28   1
#29   6
#30   1


echo "-------------------------------"

declare -A STRA
STRA["dog"]=75
echo $STRA["dog"] #dog
echo ${STRA["dog"]} #75










