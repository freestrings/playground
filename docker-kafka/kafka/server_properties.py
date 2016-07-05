import os
import os.path
import sys

kafka_home = os.environ.get('KAFKA_HOME')
if not kafka_home:
    sys.exit('KAFKA_HOME is empty')

server_properties_file = kafka_home + '/config/server.properties'
if not os.path.isfile(server_properties_file):
    sys.exit('Can not read: ' + server_properties_file)

properties = dict()
with open(server_properties_file, 'r') as f:
    while True:
        line = f.readline()
        if not line: break
        if not line.strip(): continue
        if line.startswith('#'): continue
        prop = line.split('=')
        properties[prop[0]] = prop[1].strip()

for key in os.environ:
    if not key: continue
    if key.startswith('KAFKA') and key != 'KAFKA_HOME':
        value = os.environ.get(key)
        key = key.lower().replace('kafka_', '').replace('_', '.')
        properties[key] = value

with open(server_properties_file + '.active', 'w') as f:
    for key in properties.keys():
        f.write("%s=%s\n" % (key, properties.get(key)))

