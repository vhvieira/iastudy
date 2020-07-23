#!/usr/bin/env python example
import preprocessing

mysql = {
    "host": "localhost",
    "user": "root",
    "passwd": "my secret password",
    "db": "write-math",
}
preprocessing_queue = [
    preprocessing.scale_and_center,
    preprocessing.dot_reduction,
    preprocessing.connect_lines,
]
use_anonymous = True


#using phyton example
#!/usr/bin/env python
import databaseconfig as cfg

connect(cfg.mysql["host"], cfg.mysql["user"], cfg.mysql["password"])



#Json read example
import json

with open("config.json") as json_data_file:
    data = json.load(json_data_file)
print(data)


#YML read example
import yaml

with open("config.yml", "r") as ymlfile:
    cfg = yaml.load(ymlfile)

for section in cfg:
    print(section)
print(cfg["mysql"])
print(cfg["other"])