from pathlib import Path
from classes.Configuration import Configuration
from classes.DriverFactory import DriverFactory
from classes.WhatsappWebExecutor import WhatsappWebExecutor
from classes.ConversationClient import ConversationClient
import time
import html2text

"""
This class defines a whatsapp bot to be used in Phyton
"""
class WhatsappBot:
    def __init__(self):
        config = Configuration()
        self.configMessage=config.getConfigValue("configMessage")
        self.configList=config.getConfigValue("configList")
        self.executor = WhatsappWebExecutor()
        self.driverFactory = DriverFactory()
        self.client = ConversationClient()
        
    #Load file configuration
    def LoadFileConfiguration(self, myNumber):
        messageFile = Path(self.configMessage)
        self.message = str(messageFile.read_text())
        listFile = Path(self.configList)
        self.to = list(str(listFile.read_text()).split(';'))

    #Method to send a single message to a number
    def SendMessage(self, myNumber, dest, message):
        try:
            conversation = self.executor.findConversation(dest, myNumber)
            print('Conversation found, sending message')
            self.executor.writeMessage(conversation, myNumber, message)
        except Exception as ex:
            print('Error when sending message from file '  + dest, ex)

    #Method to send a dialogflow event to a number
    def SendEvent(self, myNumber, dest, eventName):
        try:
            conversation = self.executor.findConversation(dest, myNumber)
            print('Conversation found, sending event to dialogflow')
            message = self.client.sendSimpleEvent(eventName)
            print("Response from dialogflow event: " + message)
            self.executor.writeMessage(conversation, myNumber, message)
        except Exception as ex:
            print('Error when sending message from file '  + dest, ex)

    #Method to send messages in batch
    def SendBatchMessages(self, myNumber):
        self.LoadFileConfiguration(myNumber)
        print( 'Sent list from file: ' + str(self.to) )
        for dest in self.to:
           self.SendMessage(myNumber, dest, self.message)

    def ReadNewMessages(self, myNumber):
        self.LoadFileConfiguration(myNumber)
        print( 'Receiving new messages from list: ' + str(self.to) )
        for dest in self.to:
           self.executor.ReadMessages(myNumber, dest)

    def ReadMessagesFromList(self, myNumber, list):
        self.LoadFileConfiguration(myNumber)
        print( 'Receiving new messages from list: ' + str(list) )
        for dest in list:
           self.executor.ReadMessages(myNumber, dest)

    def Setup(self, myNumber, waitTime, directory):
        print('Setup whatsapp robot')
        self.driver = self.driverFactory.getDriver(myNumber, int(waitTime))
        time.sleep(int(waitTime))
        self.driver.get_screenshot_as_file(str(directory) + "\\" + str(myNumber) +  ".png")