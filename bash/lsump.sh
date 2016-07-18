while getopts "actu:v" FLAG ; do
    case $FLAG in
    a) AFLAG=set
        ;;
    c) CFLAG=set
        ;;
    t) TFLAG=set
        ;;
    u) UVAL="$OPTARG"
        ;;
    v) VFLAG=set
        ;;
    *) echo "usage: ${0###*/} [-a] [-c] [-t] [-u option]"
       echo "example: ls -l | ${0###*/} -ac"
       exit 1
        ;;
    esac
done >&2
shift $((OPTIND -1))

declare -i COUNT=0

ls -l | tail +2 | { while read PERM LN USR GRP SIZ MON DAY YRTM FILENM
do
    if [[ ! "$FILENM" ]] ; then continue; fi
    if [[ "$UVAL" && "$USR" != "$UVAL" ]] ; then continue; fi
    COUNT+=1
    [[ $VFLAG ]] && echo "File: '$FILENM'" is $SIZ byte long
    let TOTAL+=SIZ
    (( SUM+=SIZ ))
    ALL=$((ALL+SIZ))
done


#if [[ $CFLAG=="set" ]] ; then echo $COUNT files.
[[ $CFLAG ]] && echo $COUNT files.
[[ $TFLAG ]] && echo $TOTAL $SUM $ALL

if [[ $COUNT > 0 && $AFLAG ]] ; then
    let "AVG=($TOTAL+($COUNT/2))/$COUNT"
    printf "%d bytes per file on average\n" $AVG 
fi
}
