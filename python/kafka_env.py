import os
import re

os.environ['KAFKA_BROKER_ID'] = 'aaaa'

lines = dict()
with open('/Users/freestrings/Documents/github/kafka/config/server.properties', 'r') as f:
    for line in f:
        if not line.startswith('#') and line.strip() != "":
            prop = line.split('=')
            lines[prop[0].strip()] = prop[1].strip()

reg = re.compile(r"^KAFKA")
for key in os.environ.keys():
    if reg.match(key):
        replaced = key.replace('KAFKA_', '').lower().replace('_', '.')
        lines[replaced] = os.environ.get(key)

serverPropertyFile = open('/Users/freestrings/Documents/github/kafka/config/server.properties.1', 'w')
for key in lines.keys():
    serverPropertyFile.write(key + '=' + lines.get(key) + '\n')

