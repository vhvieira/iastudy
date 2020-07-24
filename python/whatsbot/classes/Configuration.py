#YML read example
import yaml

class Configuration:
    def __init__(self):
        with open("config/config.yml", "r") as ymlfile:
            self.cfg = yaml.load(ymlfile)

    def getConfigValue(self, key):
        return self.cfg[key]

#print test for configuration
conf = Configuration()
for section in conf.cfg:
    print('key: ' + section)

print('value: ' + conf.getConfigValue('configMessage'))
print('value: ' + conf.getConfigValue('configList'))