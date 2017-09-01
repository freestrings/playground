case "$1" in
	"--list")
		ifconfig -a |awk '/^[a-z]/ { iface=$1; mac=$NF; next } /ether/ { print iface, $2}'
		;;
	"--change")
	  echo "$2" "$3"	
		sudo ifconfig ether $2 $3
		;;
	*)
		echo "--list, --change"
		exit 1
		;;
esac
