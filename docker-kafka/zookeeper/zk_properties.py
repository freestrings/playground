import os
import os.path
import sys

kafka_home = os.environ.get('KAFKA_HOME')
if not kafka_home:
    sys.exit('KAFKA_HOME is empty')

zk_properties_file = kafka_home + '/config/zookeeper.properties'
if not os.path.isfile(zk_properties_file):
    sys.exit('Can not read: ' + zk_properties_file)

properties = dict()
with open(zk_properties_file, 'r') as f:
    while True:
        line = f.readline()
        if not line: break
        if not line.strip(): continue
        if line.startswith('#'): continue
        prop = line.split('=')
        properties[prop[0]] = prop[1].strip()

for key in os.environ:
    if not key: continue
    if key.startswith('ZK') and key != 'KAFKA_HOME':
        value = os.environ.get(key)
        key = key.lower().replace('zk_', '')
        key = ''.join(x.capitalize() or '_' for x in key.split('_'))
        key = key[0].lower() + key[1:]
        properties[key] = value

with open(zk_properties_file + '.active', 'w') as f:
    for key in properties.keys():
        f.write("%s=%s\n" % (key, properties.get(key)))
