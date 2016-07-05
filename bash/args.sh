echo "'$#'" $#
echo $1
echo ${1}
echo '1st ($1) is "'$1'"'
echo "1st $1) is '$1'"

for anarg
do
    echo "#"$anarg
    echo ${#anarg}
done
