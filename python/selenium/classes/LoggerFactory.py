#Logger factory
import logging
from logging.config import fileConfig
import datetime

class LoggerFactory:
    #default log config file
    configFile = "config/logconfig.ini"
    
    #loads default file
    def __init__(self):
        fileConfig(LoggerFactory.configFile)

    #get the logger for the given number
    def getLogger(self, myNumber):
        if(myNumber is None):
            myNumber = str(datetime.datetime.now().strftime('%d%m%Y'))
        #logging.basicConfig(filename=str(myNumber) +'.log', level=logging.DEBUG, format='%(asctime)s | %(name)s | %(levelname)s | %(message)s')
        logging.basicConfig(filename=str(myNumber) +'.log')
        return logging.getLogger()

    #Method that reads a custom file for log configuration
    def loadCustomFile(self, filePath):
        LoggerFactory.configFile = filePath
        fileConfig(LoggerFactory.configFile)

#print test for configuration
factory = LoggerFactory()
log = factory.getLogger(None)
log.debug('DEBUG')
log.info('INFO')
log.warning('WARNING')
log.error('ERROR')
log.critical('CRITICAL')