from selenium import webdriver
from pathlib import Path
from classes.Configuration import Configuration
import time

"""
This class defines a whatsapp bot to be used in Phyton
"""
class WhatsappBot:
    def __init__(self):
        print("Loading bot configuration")
        config = Configuration()
        self.configMessage=config.getConfigValue("configMessage")
        self.configList=config.getConfigValue("configList")
        self.loadSleep=int(config.getConfigValue("loadSleep"))
        self.smallInterval=int(config.getConfigValue("shortSleep"))
        self.longInterval=int(config.getConfigValue("longSleep"))
        self.lang=config.getConfigValue("lang")
        self.data_dir=config.getConfigValue("data_dir")
        self.url=config.getConfigValue("whatsapp_url")
        self.inputClass=config.getConfigValue("inputChatClass")
        self.xPathForChat=config.getConfigValue("xPathForChat")
        self.xPathForSendBtn=config.getConfigValue("xPathForSendBtn")
        self.xPathForMainDiv=config.getConfigValue("xPathForMainDiv")
        
    #Load configuration
    def LoadConfiguration(self):
        messageFile = Path(self.configMessage)
        self.message = str(messageFile.read_text())
        listFile = Path(self.configList)
        self.message = str(messageFile.read_text())
        self.to = list(str(listFile.read_text()).split(';'))
        self.options = webdriver.ChromeOptions()
        self.options.add_argument("lang=" + self.lang)
        self.options.add_argument("user-data-dir=" + self.data_dir)
        self.driver =  webdriver.Chrome(executable_path=r'./bin/chromedriver.exe', options=self.options)    

    #Method to send message
    def SendMessages(self):
        self.LoadConfiguration()
        print( 'Lista de destinatarios: ' + str(self.to) )
        self.driver.get(self.url)
        time.sleep(self.loadSleep)
        #If we want to keep reading message, we can create a infinity loop here
        #Conversations are class div class="_210SC"
        for dest in self.to:
            print('Enviando mensagem para:' + dest)
            #[0] is search in the conversation, [1] is chatbox
            chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
            time.sleep(self.smallInterval)
            print('Chatboxs encontrados: ' + str(len(chatboxes)))
            chatboxes[0].click()
            chatboxes[0].send_keys(dest)
            time.sleep(self.longInterval)
            participantList = self.driver.find_element_by_xpath(self.xPathForMainDiv)
            print('Lista de conversas encontrada')
            time.sleep(self.smallInterval)
            conversation = participantList.find_element_by_xpath(self.xPathForChat.replace("{name}", dest))
            time.sleep(self.smallInterval)
            print('Conversa encontrada')
            conversation.click()
            #[0] is search in the conversation, [1] is chatbox
            chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
            time.sleep(self.smallInterval)
            print('Chatboxs encontrados: ' + str(len(chatboxes)))
            chatboxes[1].click()
            chatboxes[1].send_keys(self.message)
            send_icon= self.driver.find_element_by_xpath(self.xPathForSendBtn)
            time.sleep(self.smallInterval)
            print('Send icon encontrado')
            send_icon.click()
            print('Mensagem enviada para:' + dest)
            time.sleep(self.longInterval)

    def Setup(self):
        print('Setup whatsapp robot')
        self.LoadConfiguration()
        self.driver.get(self.url)

    def TestMessage(self):
        config = Configuration()
        self.configList=config.getConfigValue("testList")
        self.configMessage=config.getConfigValue("testMessage")
        self.SendMessages()