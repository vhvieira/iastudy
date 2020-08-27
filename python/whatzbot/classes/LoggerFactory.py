#Logger factory
import logging
from logging.config import fileConfig
import datetime

class LoggerFactory:
    configFile = "config/logconfig.ini"
    #def __init__(self):
        #fileConfig(LoggerFactory.configFile)

    #TODO: Implement log in file configuration for production
    def getLogger(self, myNumber):
        if(myNumber is None):
            myNumber = str(datetime.datetime.now().strftime('%d%m%Y'))
        #logging.basicConfig(filename=str(myNumber) +'.log', level=logging.DEBUG, format='%(asctime)s | %(name)s | %(levelname)s | %(message)s')
        logging.basicConfig(filename=str(myNumber) +'.log')
        return logging.getLogger()

    def loadCustomFile(self, filePath):
        LoggerFactory.configFile = filePath
        #fileConfig(LoggerFactory.configFile)

#print test for configuration
factory = LoggerFactory()
log = factory.getLogger(None)
log.debug('DEBUG')
log.info('INFO')
log.warning('WARNING')
log.error('ERROR')
log.critical('CRITICAL')