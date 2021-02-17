#YML read example
import yaml

class Configuration:
    configFile = "config/config.yml"
    def __init__(self):
        with open(Configuration.configFile, "r") as ymlfile:
            self.cfg = yaml.load(ymlfile)

    def getConfigValue(self, key):
        return str(self.cfg[key])

    def loadCustomFile(self, filePath):
        Configuration.configFile = filePath
        with open(Configuration.configFile, "r") as ymlfile:
            self.cfg = yaml.load(ymlfile)

#print test for configuration
conf = Configuration()
for section in conf.cfg:
    print('key: ' + section)

print('value: ' + conf.getConfigValue('configMessage'))
print('value: ' + conf.getConfigValue('configList'))