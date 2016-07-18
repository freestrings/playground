declare -i COUNT=0

while read PERM LN USR GRP SIZ MON DAY YRTM FILENM
do
    if [[ ! "$FILENM" ]] ; then continue; fi
    COUNT+=1
    echo "File: '$FILENM'" is $SIZ byte long
    let TOTAL+=SIZ
    (( SUM+=SIZ ))
    ALL=$((ALL+SIZ))
done

echo $COUNT files.
echo $TOTAL $SUM $ALL

