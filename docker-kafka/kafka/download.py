import os
import sys
import urllib2
import json

scala_version = os.environ.get('SCALA_VERSION')
if not scala_version:
    sys.exit("SCALA_VERSION is empty")

kafka_version = os.environ.get('KAFKA_VERSION')
if not kafka_version:
    sys.exit("KAFKA_VERSION is empty")

meta_url = 'https://www.apache.org/dyn/closer.cgi\?as_json=1'
meta_info = urllib2.urlopen(meta_url).read()
mirror = json.loads(meta_info)['preferred']
tarball_url = mirror + ("kafka/%s/kafka_%s-%s.tgz" % (kafka_version, scala_version, kafka_version))
print "Tarball: " + tarball_url

response = urllib2.urlopen(tarball_url)
f = open("/tmp/kafka_%s-%s.tgz" % (scala_version, kafka_version), 'w') 

CHUNK = 16*1024
while True:
    chunk = response.read(CHUNK)
    if not chunk: break
    f.write(chunk)
f.close()

print "Downloaded: " + tarball_url
