from classes.Configuration import Configuration
from classes.DriverFactory import DriverFactory
from classes.WhatsappWebExecutor import WhatsappWebExecutor
from classes.ConversationClient import ConversationClient
from classes.FileUtils import FileUtils
from classes.LoggerFactory import LoggerFactory
import time
import html2text

"""
This class defines a whatsapp bot to be used in Phyton
"""
class WhatsappBot:

    #initializing configuration
    def __init__(self):
        config = Configuration()
        self.fileUtils = FileUtils()
        self.configMessage=config.getConfigValue("configMessage")
        self.configList=config.getConfigValue("configList")
        self.executor = WhatsappWebExecutor()
        self.driverFactory = DriverFactory()
        self.client = ConversationClient()
        self.loggerFactory = LoggerFactory()
        
    #Load file configuration
    def LoadFileConfiguration(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            self.message = self.fileUtils.readFile(myNumber, self.configMessage)
            self.to = list(self.fileUtils.readFile(myNumber, self.configList).split(';'))
        except Exception as ex:
            log.error('Error when try to load configuration (LoadFileConfiguration) for number: '  + myNumber, ex)

    #Method to send a single message to a number
    def SendMessage(self, myNumber, dest, message):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            conversation = self.executor.findConversation(dest, myNumber)
            if(conversation is not None):
                log.debug('Conversation found, sending message')
                self.executor.writeMessage(conversation, myNumber, message)
        except Exception as ex:
            log.error('Error when sending message (SendMessage) for number: '  + dest, ex)

    #Method to send a dialogflow event to a number
    def SendEvent(self, myNumber, dest, eventName):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            conversation = self.executor.findConversation(dest, myNumber)
            if(conversation is not None):
                log.debug('Conversation found, sending event to dialogflow')
                message = self.client.sendSimpleEvent(eventName)
                log.debug("Response from dialogflow event: " + message)
                self.executor.writeMessage(conversation, myNumber, message)
        except Exception as ex:
            log.error('Error when sending event (SendEvent) for number: '  + dest, ex)

    #Method to send messages in batch
    def SendBatchMessages(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log = self.loggerFactory.getLogger(myNumber)
            self.LoadFileConfiguration(myNumber)
            log.debug( 'Sent list from file: ' + str(self.to) )
            for dest in self.to:
                self.SendMessage(myNumber, dest, self.message)
        except Exception as ex:
            log.error('Error when try to send new messages (SendBatchMessages) for number: '  + myNumber, ex) 

    #Method that read new messages using the list.txt file in config
    def ReadNewMessages(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            self.LoadFileConfiguration(myNumber)
            log.debug( 'Receiving new messages from list: ' + str(self.to) )
            for dest in self.to:
                self.executor.ReadMessages(myNumber, dest)
        except Exception as ex:
            log.error('Error when try to read new messages (ReadNewMessages) for number: '  + myNumber, ex) 

    #Method that read new messages using the list.txt file in config
    def LoadLatestAudio(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            self.LoadFileConfiguration(myNumber)
            log.debug( 'Loading audios from list: ' + str(list) )
            for dest in self.to:
                self.executor.loadLastConversationAudio(myNumber, dest)
        except Exception as ex:
            log.error('Error when try to read lastest audios (LoadLatestAudio) for number: '  + myNumber, ex) 

    #Method that read new messages using the received list of numbers
    def ReadMessagesFromList(self, myNumber, list):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug( 'Receiving new messages from list: ' + str(list) )
            for dest in list:
                self.executor.ReadMessages(myNumber, dest)
        except Exception as ex:
            log.error('Error when try to read new messages (ReadMessagesFromList) for number: '  + myNumber, ex) 

    #Method that read new messages using the received list of numbers
    def LoadLatestAudioFromList(self, myNumber, list):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug( 'Loading audios from list: ' + str(list) )
            for dest in list:
                self.executor.loadLastConversationAudio(myNumber, dest)
        except Exception as ex:
            log.error('Error when try to read lastest audios (LoadLatestAudioFromList) for number: '  + myNumber, ex) 
    
    #Method that read new messages using the received list of numbers
    def UnloadLatestAudioFromList(self, myNumber, list):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug( 'Removing audios from list: ' + str(list) )
            for dest in list:
                self.executor.unloadLastConversationAudio(myNumber, dest)
        except Exception as ex:
            log.error('Error when try to remove lastest audios (UnloadLatestAudioFromList) for number: '  + myNumber, ex) 

    #Method that setup a new Whatsweb web and store in chrome profile
    def Setup(self, myNumber, waitTime, directory):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug('Setup whatsapp robot')
            self.driver = self.driverFactory.getDriver(myNumber, int(waitTime))
            time.sleep(int(waitTime))
            self.driver.get_screenshot_as_file(str(directory) + "\\" + str(myNumber) +  ".png")
        except Exception as ex:
            log.error('Error when try to perform setup (Setup) for number: '  + myNumber, ex)

    #Test method
    def Test(self, myNumber):
        try:
            text = "Hi"
            self.executor.sendMessage(myNumber, text)
        except Exception as ex:
            log = self.loggerFactory.getLogger(myNumber)
            log.error('Error on Test method for number: '  + myNumber, ex)