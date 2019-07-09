docker run -it --rm --name syslog \
	-e SYSLOG_USERNAME=admin \
	-e SYSLOG_PASSWORD=1234 \
	-p 8514:80 \
	-p 514:514/udp pbertera/syslogserver

