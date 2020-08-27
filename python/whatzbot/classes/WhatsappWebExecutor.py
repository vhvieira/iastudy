from selenium import webdriver
from classes.Configuration import Configuration
from classes.DriverFactory import DriverFactory
from classes.ConversationClient import ConversationClient
from classes.ConversationFactory import ConversationFactory
from classes.LoggerFactory import LoggerFactory
import time
import datetime
import html2text

"""
This class defines is a mapping using Selinium Web Driver for the whatsapp web page
"""
class WhatsappWebExecutor:
    def __init__(self):
        config = Configuration()
        self.configMessage=config.getConfigValue("configMessage")
        self.configList=config.getConfigValue("configList")
        self.smallInterval=int(config.getConfigValue("shortSleep"))
        self.longInterval=int(config.getConfigValue("longSleep"))
        self.loadSleep=int(config.getConfigValue("loadSleep"))
        self.url=config.getConfigValue("whatsapp_url")
        self.inputClass=config.getConfigValue("inputChatClass")
        self.xPathForChat=config.getConfigValue("xPathForChat")
        self.xPathForSendBtn=config.getConfigValue("xPathForSendBtn")
        self.xPathForLefPane=config.getConfigValue("xPathForLefPane")
        self.xPathForNonRead=config.getConfigValue("xPathForNonRead")
        self.xPathForConversation=config.getConfigValue("xPathForConversation")
        self.xPathForListener=config.getConfigValue("xPathForListener")
        self.xPathForMessages=config.getConfigValue("xPathForMessages")
        self.xPathForAudios=config.getConfigValue("xPathForAudios")
        self.date_formated=datetime.datetime.now().strftime("%d/%m/%Y")
        self.errorMessage=config.getConfigValue("errorMessage")
        self.welcomeEvent=config.getConfigValue("welcomeEvent")
        self.stopContact=config.getConfigValue("stopContact")
        #local instances
        self.driverFactory=DriverFactory()
        self.client = ConversationClient()  
        self.conversation = ConversationFactory() 
        self.loggerFactory = LoggerFactory()
        #for audio messages
        self.audioURLs = {'nbr' : {'url'}}

    #Method to find the a specific conversation
    def findConversation(self, dest, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        log.debug('Sending message to:' + dest) 
        #[0] is search in the conversation, [1] is chatbox
        chatboxes = driver.find_elements_by_class_name(self.inputClass)
        chatboxes[0].click()
        chatboxes[0].clear()
        chatboxes[0].send_keys(dest)
        time.sleep(self.smallInterval)
        participantList = driver.find_element_by_xpath(self.xPathForLefPane)
        conversation = participantList.find_element_by_xpath(self.xPathForChat.replace("{name}", dest))
        return conversation

    #Method to send message to the provided conversation
    def writeMessage(self, conversation, myNumber, message):
        log = self.loggerFactory.getLogger(myNumber)
        log.debug('writing message message to:' + str(conversation)) 
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        conversation.click()
        time.sleep(self.smallInterval)
        #[0] is search in the conversation, [1] is chatbox
        chatboxes = driver.find_elements_by_class_name(self.inputClass)
        log.debug('Chatbox found:' + str(chatboxes[1])) 
        chatboxes[1].click()
        chatboxes[1].clear()
        chatboxes[1].send_keys(message)
        log.debug('Message sent:' + message) 
        send_icon= driver.find_element_by_xpath(self.xPathForSendBtn)
        send_icon.click()
        time.sleep(self.smallInterval)
        #call go to stop conversation
        self.goToStopContact(myNumber)
    
    #move the cursor to a stop contact (that will never send me messages)
    def goToStopContact(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        log.debug('going to stop contact:' + self.stopContact) 
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        leftPane = driver.find_element_by_xpath(self.xPathForLefPane)
        participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
        stopConversation = participantList.find_element_by_xpath(self.xPathForListener.replace("{name}", self.stopContact))
        stopConversation.click()
        time.sleep(self.smallInterval)

    def sendMessageToAPI(self, destNumber, message):
        try:          
            log = self.loggerFactory.getLogger(myNumber)
            log.debug('Text to be sent to dialogflow api is: ' + message)
            if message is None:
                self.conversation.createNew(destNumber)
                response = self.client.sendSimpleEvent(self.welcomeEvent)
            else: 
                conversationID = self.conversation.getConversationID(destNumber)
                response = html2text.html2text(self.client.sendContinuousMessage(conversationID, message)).strip()
            log.debug( "Dialogflow response: " + str(response))
            return response
        except Exception as ex:
            log.error('Error sending payload to conversation-api', ex)
            return self.errorMessage

    #Method that will get last text message and send to conversation-api
    def processLastTextMessage(self, myNumber, conversation, dest):
        log = self.loggerFactory.getLogger(myNumber)
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        conversationDiv = driver.find_element_by_xpath("//div[@id='main']")
        log.debug('Conversation found, getting texts')
        texts = conversationDiv.find_elements_by_xpath(self.xPathForMessages.replace("{today}", self.date_formated))
        if(len(texts) > 0):
            text = html2text.html2text(texts[-1].text).strip()    
        #call conversation-api
        response = self.sendMessageToAPI(dest, text)    
        self.writeMessage(conversation, myNumber, response)

    def hasNewAudios(self, myNumber, conversation, dest):
        log = self.loggerFactory.getLogger(myNumber)
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        conversationDiv = driver.find_element_by_xpath("//div[@id='main']")
        log.debug('Conversation found, getting texts')
        audios = conversationDiv.find_elements_by_xpath(self.xPathForAudios)
        if(len(audios) > 0):
            url = audios[-1].src
            if(url is not None and self.audioURLs.get(dest) is None):
                return True
            else:
                if(url != self.audioURLs.get(dest)):
                    return True
        #default return is false
        return false

    #Method that read messages from a list and try to find new messages and process them
    def ReadMessages(self, myNumber, dest):
        log = self.loggerFactory.getLogger(myNumber)
        log.debug('Starting read messages')
        driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
        leftPane = driver.find_element_by_xpath(self.xPathForLefPane)
        participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
        try:
            log.debug('Reading new messages of pattern:' + dest)
            conversations = participantList.find_elements_by_xpath(self.xPathForListener.replace("{name}", dest))
            for conversation in conversations:
                log.debug('Reading new messsages for ' + conversation.text)
                newConversation = conversation.find_element_by_xpath(self.xPathForNonRead.replace("{name}", dest))
                log.debug('New conversation: ' + str(newConversation))
                if(newConversation is not None):
                    conversation.click()
                    time.sleep(self.smallInterval)
                    if( self.hasNewAudios(myNumber, conversation, dest) ):
                        log.info('It is a audio file, need to implement download and api call for it')
                    else:
                        log.info('There is no new audio, getting the latest text')
                        self.processLastTextMessage(myNumber, conversation, dest)
                    #call go to stop conversation
                    self.goToStopContact(myNumber)                 
        except Exception as ex:
            log.warning('Error getting new messages', ex)