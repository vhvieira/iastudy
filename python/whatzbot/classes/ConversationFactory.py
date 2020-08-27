from classes.Configuration import Configuration
from classes.LoggerFactory import LoggerFactory
import datetime
from dateutil.relativedelta import relativedelta

"""
This class is the selenium driver factory
"""
class ConversationFactory:
    def __init__(self):
        config = Configuration()
        self.loggerFactory = LoggerFactory()
        self.my_conversations={'default' : '001'}
        self.conversationInterval=int(config.getConfigValue("conversationInterval"))
        self.conversationIdSeparator=config.getConfigValue("conversationIdSeparator")
        self.conversationIdPrefix=config.getConfigValue("conversationIdPrefix")
        self.conversationDateFormat=config.getConfigValue("conversationDateFormat")

    #create new conversationID
    def createNew(self, destNumber):
        twentyminsfromnow = datetime.datetime.now() + relativedelta(minutes=20)
        self.my_conversations[destNumber]=twentyminsfromnow
        return self.my_conversations.get(destNumber)

    #get the existing conversation time, or if expired then create a new one
    def getConversationTime(self, myNumber, destNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            date_now = datetime.datetime.now()
            log.debug('Current date: ' + str(date_now))
            log.debug('Found conversationID: ' + str(self.my_conversations.get(destNumber)))
            if self.my_conversations.get(destNumber) is not None:
                if(date_now < self.my_conversations.get(destNumber)):
                    log.debug('Reusing same conversationID')
                    return self.my_conversations.get(destNumber)
                else:
                    log.debug('Expired, creating new conversationID')
                    return self.createNew(destNumber)
            else:
                return self.createNew(destNumber)
        except Exception as ex:
            log.error('Error when try to create conversationID (processPostRequest) for number: '  + destNumber, ex)
            return None

    #get the complete conversationID or create a new one (official method to be called)
    def getConversationID(self, myNumber, destNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            date_id = self.getConversationTime(myNumber, destNumber)
            conversationId = self.conversationIdPrefix + destNumber + self.conversationIdSeparator + str(date_id.strftime(self.conversationDateFormat))
            log.debug('Conversation ID created: ' + conversationId)
            return conversationId
        except Exception as ex:
            log.error('Error when try to create conversationID (processPostRequest) for number: '  + destNumber, ex)
            return None