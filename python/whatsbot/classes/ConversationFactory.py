from classes.Configuration import Configuration
import datetime
from dateutil.relativedelta import relativedelta

"""
This class is the selenium driver factory
"""
class ConversationFactory:
    def __init__(self):
        config = Configuration()
        self.my_conversations={'default' : '001'}
        self.conversationInterval=int(config.getConfigValue("conversationInterval"))
        self.conversationIdSeparator=config.getConfigValue("conversationIdSeparator")
        self.conversationIdPrefix=config.getConfigValue("conversationIdPrefix")
        self.conversationDateFormat=config.getConfigValue("conversationDateFormat")

    def createNew(self, destNumber):
        twentyminsfromnow = datetime.datetime.now() + relativedelta(minutes=20)
        self.my_conversations[destNumber]=twentyminsfromnow
        return self.my_conversations.get(destNumber)

    def getConversationTime(self, destNumber):
        date_now = datetime.datetime.now()
        print('Current date: ' + str(date_now))
        print('Found conversationID: ' + str(self.my_conversations.get(destNumber)))
        if self.my_conversations.get(destNumber) is not None:
            if(date_now < self.my_conversations.get(destNumber)):
                print('Reusing same conversationID')
                return self.my_conversations.get(destNumber)
            else:
                print('Expired, creating new conversationID')
                return self.createNew(destNumber)
        else:
            return self.createNew(destNumber)

    def getConversationID(self, destNumber):
        date_id = self.getConversationTime(destNumber)
        conversationId = self.conversationIdPrefix + destNumber + self.conversationIdSeparator + str(date_id.strftime(self.conversationDateFormat))
        print('Conversation ID created: ' + conversationId)
        return conversationId