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

    def createNew(self, myNumber):
        twentyminsfromnow = datetime.datetime.now() + relativedelta(minutes=20)
        self.my_conversations[myNumber]=twentyminsfromnow
        return self.my_conversations.get(myNumber)

    def getConversationTime(self, myNumber):
        date_now = datetime.datetime.now()
        if self.my_conversations.get(myNumber) is not None:
            if(date_now < self.my_conversations[myNumber]):
                return self.my_conversations.get(myNumber)
            else:
                return self.createNew(myNumber)
        else:
            return self.createNew(myNumber)

    def getConversationID(self, myNumber):
        date_id = self.getConversationTime(myNumber)
        conversationId = self.conversationIdPrefix + myNumber + self.conversationIdSeparator + str(date_id.strftime(self.conversationDateFormat))
        print('Conversation ID created: ' + conversationId)
        return conversationId