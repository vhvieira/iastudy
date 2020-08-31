from selenium import webdriver
from classes.Configuration import Configuration
from classes.DriverFactory import DriverFactory
from classes.ConversationClient import ConversationClient
from classes.ConversationFactory import ConversationFactory
from classes.LoggerFactory import LoggerFactory
from classes.FileUtils import FileUtils
from selenium.webdriver.common.keys import Keys
import time
import datetime
import html2text

"""
This class defines is a mapping using Selinium Web Driver for the whatsapp web page
"""
class WhatsappWebExecutor:

    #Init configuration
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
        self.dateFormatForTexts=config.getConfigValue("dateFormatForTexts")
        self.date_formated=self.dateFormatForTexts.format(dt=datetime.datetime.now())
        self.errorMessage=config.getConfigValue("errorMessage")
        self.welcomeEvent=config.getConfigValue("welcomeEvent")
        self.stopContact=config.getConfigValue("stopContact")
        #local instances
        self.driverFactory=DriverFactory()
        self.client = ConversationClient()  
        self.conversation = ConversationFactory() 
        self.loggerFactory = LoggerFactory()
        self.fileUtils = FileUtils()
        #for audio messages
        self.audioURLs = {'nbr' : {'url'}}

    #Method to find the a specific conversation
    def findConversation(self, dest, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
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
        except Exception as ex:
            log.error('Error when trying to find a conversation (findConversation)', ex)
            return None

    #Method to send message to the provided conversation
    def writeMessage(self, conversation, myNumber, message):
        log = self.loggerFactory.getLogger(myNumber)
        try:
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
        except Exception as ex:
            log.error('Error when trying to write message (writeMessage)', ex)

    #move the cursor to a stop contact (that will never send me messages)
    def goToStopContact(self, myNumber):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
            #clear search field if not clear it
            chatboxes = driver.find_elements_by_class_name(self.inputClass)
            if(chatboxes is not None and html2text.html2text(chatboxes[0].text).strip() is not None):
                chatboxes[0].clear()
                chatboxes[0].send_keys(Keys.RETURN)
                time.sleep(self.smallInterval)
            log.debug('going to stop contact:' + self.stopContact) 
            leftPane = driver.find_element_by_xpath(self.xPathForLefPane)
            participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
            stopConversation = participantList.find_element_by_xpath(self.xPathForListener.replace("{name}", self.stopContact))
            stopConversation.click()
            time.sleep(self.smallInterval)
        except Exception as ex:
            log.error('Error going to stop contact (goToStopContact)', ex)

    #Method that send the message to conversation-api
    def sendMessageToAPI(self, myNumber, destNumber, message, audioFile):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            conversationID = self.conversation.getConversationID(myNumber, destNumber)
            if audioFile is None:
                log.debug('Text to be sent to dialogflow api is: ' + message)
                if message is None:
                    self.conversation.createNew(destNumber)
                    response = self.client.sendSimpleEvent(self.welcomeEvent)
                else: 
                    response = html2text.html2text(self.client.sendContinuousMessage(conversationID, message)).strip()
            else:
                log.debug('Sending audio to conversation: ' + conversationID)
                response = self.client.sendContinuosAudio(conversationID, audioFile)
            log.debug( "Dialogflow response: " + str(response))
            return response
        except Exception as ex:
            log.error('Error sending payload to conversation-api', ex)
            return self.errorMessage

    #Method that will get last text message and send to conversation-api
    def processLastTextMessage(self, myNumber, conversation, dest):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
            conversationDiv = driver.find_element_by_xpath("//div[@id='main']")
            log.debug('Conversation found, getting texts')
            texts = conversationDiv.find_elements_by_xpath(self.xPathForMessages.replace("{today}", self.date_formated))
            if(len(texts) > 0):
                text = html2text.html2text(texts[-1].text).strip()    
            #call conversation-api
            response = self.sendMessageToAPI(myNumber, dest, text, None)    
            self.writeMessage(conversation, myNumber, response)
        except Exception as ex:
            log.error('Error while processing new text messages (processLastTextMessage)', ex)
            return False

    #check if conversation has a new audio or not
    def hasNewAudio(self, myNumber, conversation, dest):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
            conversationDiv = driver.find_element_by_xpath("//div[@id='main']")
            log.debug('Conversation found, getting texts')
            audios = conversationDiv.find_elements_by_xpath(self.xPathForAudios)
            if(len(audios) > 0):
                audio = audios[-1]
                audio_src = audio.get_attribute("src")
                log.debug('Audio element found' + str(audio))
                log.debug( '>>> scr found: ' + str(audio_src) )
                if(audio_src is not None and self.audioURLs.get(dest) is None):
                    log.debug('First audio for the number, should be loaded on startup and ignored')
                    self.audioURLs[dest] = audio_src
                    return True
                else:
                    log.debug('Not first audio for the number, should be verified')
                    if(audio_src == self.audioURLs.get(dest)):
                        log.debug('Old audio, should be ignored')
                        return False
                    else:
                        log.debug('New audio, should be processed')
                        self.audioURLs[dest] = audio_src
                        return True
            #default return is false
            return False
        except Exception as ex:
            log.error('Error while reading new audio files (hasNewAudios)', ex)
            return False

     #Method that will process and send audio file to conversation-api
    def processLastAudioMessage(self, myNumber, conversation, dest, audioFile):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            #call conversation-api
            response = self.sendMessageToAPI(myNumber, dest, None, audioFile)    
            self.writeMessage(conversation, myNumber, response)
        except Exception as ex:
            log.error('Error while processing new text messages (processLastTextMessage)', ex)

    #load last audio when conversation listener starts
    def loadLastConversationAudio(self, myNumber, dest):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            conversation = self.findConversation(dest, myNumber)
            if(conversation is not None):
                conversation.click()
                time.sleep(self.smallInterval)
                if( self.hasNewAudio(myNumber, conversation, dest) ):
                    log.info('Latest audio file was sucessfully loaded')
                else:
                    log.info('There is no new audio in this conversation to be loaded')
            #always go to stop contact
            self.goToStopContact(myNumber)  
        except Exception as ex:
            log.error('Error while reading new audio files (hasNewAudios)', ex)

    #unload last audio
    def unloadLastConversationAudio(self, myNumber, dest):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            if(self.audioURLs.get(dest) is not None):
                self.audioURLs.remove(dest)
                log.info('Latest audio file was sucessfully unloaded')
            else:
                self.audioURLs.remove(dest)
                log.info('There is audio in this conversation to be unloaded') 
        except Exception as ex:
            log.error('Error while removing audio cache (loadLastConversationAudio)', ex) 

    #Method that read messages from a list and try to find new messages and process them
    def ReadMessages(self, myNumber, dest):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug('Starting read messages')
            driver = self.driverFactory.getDriver(myNumber, self.loadSleep)
            leftPane = driver.find_element_by_xpath(self.xPathForLefPane)
            participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
            log.debug('Reading new messages of pattern:' + dest)
            conversations = participantList.find_elements_by_xpath(self.xPathForListener.replace("{name}", dest))
            for conversation in conversations:
                log.debug('Reading new messsages for ' + conversation.text)
                newConversation = conversation.find_element_by_xpath(self.xPathForNonRead.replace("{name}", dest))
                log.debug('New conversation: ' + str(newConversation))
                if(newConversation is not None):
                    conversation.click()
                    time.sleep(self.smallInterval)
                    if( self.hasNewAudio(myNumber, conversation, dest) ):
                        log.info('It is a audio file, need to implement download and api call for it')
                        audioFile = self.fileUtils.downloadFileWithDriver(myNumber, dest, self.audioURLs.get(dest), driver)
                        self.processLastAudioMessage(myNumber, conversation, dest, audioFile)
                    else:
                        log.info('There is no new audio, getting the latest text')
                        self.processLastTextMessage(myNumber, conversation, dest)
                    #call go to stop conversation
                    self.goToStopContact(myNumber)                 
        except Exception as ex:
            log.warning('Error getting new messages', ex)