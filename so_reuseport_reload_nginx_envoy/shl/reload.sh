iptables -I INPUT -p tcp --dport 80 --syn -j DROP
/shl/nginx/stop.sh 1
sleep 1
iptables -D INPUT -p tcp --dport 80 --syn -j DROP
