from selenium import webdriver
from pathlib import Path
from classes.Configuration import Configuration
from classes.ConversationClient import ConversationClient
import time
import html2text

"""
This class defines a whatsapp bot to be used in Phyton
TODO: Create class driver manage to encapsulate selenium drive instances
TODO: Use ConversationClient.py to call conversation API
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
        self.xPathForLefPane=config.getConfigValue("xPathForLefPane")
        self.xPathForNonRead=config.getConfigValue("xPathForNonRead")
        self.xPathForConversation=config.getConfigValue("xPathForConversation")
        self.xPathForListener=config.getConfigValue("xPathForListener")
        
    #Load configuration
    def LoadConfiguration(self, myNumber):
        messageFile = Path(self.configMessage)
        self.message = str(messageFile.read_text())
        listFile = Path(self.configList)
        self.message = str(messageFile.read_text())
        self.to = list(str(listFile.read_text()).split(';'))
        self.options = webdriver.ChromeOptions()
        self.options.add_argument("lang=" + self.lang)
        self.options.add_argument("user-data-dir=" + self.data_dir + str(myNumber))
        self.driver =  webdriver.Chrome(executable_path=r'./bin/chromedriver.exe', options=self.options)    

    #Method to send message
    def SendMessages(self, waitTime, myNumber):
        self.LoadConfiguration(myNumber)
        print( 'Lista de destinatarios: ' + str(self.to) )
        self.driver.get(self.url)
        time.sleep(int(waitTime))
        #If we want to keep reading message, we can create a infinity loop here
        #Conversations are class div class="_210SC"
        for dest in self.to:
            print('Enviando mensagem para:' + dest)
            try:
                #[0] is search in the conversation, [1] is chatbox
                chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
                time.sleep(self.smallInterval)
                print('Chatboxs encontrados: ' + str(len(chatboxes)))
                chatboxes[0].click()
                chatboxes[0].clear()
                chatboxes[0].send_keys(dest)
                time.sleep(self.longInterval)
                participantList = self.driver.find_element_by_xpath(self.xPathForLefPane)
                print('Lista de conversas encontrada')
                time.sleep(self.smallInterval)
                ##TODO Refactor to extract method here and add elements and a new for --> Functional call
                conversation = participantList.find_element_by_xpath(self.xPathForChat.replace("{name}", dest))
                time.sleep(self.smallInterval)
                print('Conversa encontrada')
                conversation.click()
                #[0] is search in the conversation, [1] is chatbox
                chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
                time.sleep(self.smallInterval)
                print('Chatboxs encontrados: ' + str(len(chatboxes)))
                chatboxes[1].click()
                chatboxes[1].clear()
                chatboxes[1].send_keys(self.message)
                send_icon= self.driver.find_element_by_xpath(self.xPathForSendBtn)
                time.sleep(self.smallInterval)
                print('Send icon encontrado')
                send_icon.click()
                print('Mensagem enviada para:' + dest)
                time.sleep(self.longInterval)
            except Exception as ex:
                print('Erro enviando mensage para: '  + dest, ex)

    #TODO: Refactor this for better performance
    def ReadMessages(self):
        print('Iniciando read messages')
        self.LoadConfiguration()
        self.driver.get(self.url)
        time.sleep(self.loadSleep)
        leftPane = self.driver.find_element_by_xpath(self.xPathForLefPane)
        participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
        print('Lista de conversas encontrada')
        #Infinity loop for reading new messages
        print('Loop para leitura de mensagens...')
        #Finding for the listening phone list
        for dest in self.to:
            try:
                print('Reading novas conversas para:' + dest)
                conversations = participantList.find_elements_by_xpath(self.xPathForListener.replace("{name}", dest))
                print('Conversas encontradas: ' + str(len(conversations)))
                time.sleep(self.smallInterval)
                for conversation in conversations:
                    print('Conversa encontrada --> ' + conversation.text)
                    ##TODO: Starts from new message them try to find the title or do ../../ click
                    newConversation = conversation.find_element_by_xpath(self.xPathForNonRead.replace("{name}", dest))
                    print('Nova conversa: ' + str(newConversation) + ' - txt: ' + newConversation.text)
                    conversation.click()
                    time.sleep(self.smallInterval)
                    conversationDiv = self.driver.find_element_by_xpath("//div[@id='main']")
                    print('Conversa encontrada, buscando textos')
                    time.sleep(self.smallInterval)
                    #TODO This should be a functional call (reading texts) - saving latest one the number
                    texts = conversationDiv.find_elements_by_xpath(".//*[contains(@data-pre-plain-text,'27/07/2020')]")
                    for text in texts:
                        print( html2text.html2text(text.get_attribute("data-pre-plain-text")) + html2text.html2text(text.text) )
                        client = ConversationClient()
                        print( "Sending to dialogflow: " + text.text)
                        response = client.sendSimpleMessage(text.text)
                        chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
                        print('Chatboxs encontrados: ' + str(len(chatboxes)))
                        chatboxes[1].clear()
                        chatboxes[1].send_keys(response)
                        chatboxes[1].click()
            except Exception as ex:
                print('Não foram encontradas novas mensagens', ex)
            time.sleep(self.longInterval) 

    def Setup(self, waitTime, myNumber, directory):
        print('Setup whatsapp robot')
        self.LoadConfiguration(myNumber)
        self.driver.get(self.url)
        time.sleep(int(waitTime))
        self.driver.get_screenshot_as_file(str(directory) + "\\" + str(myNumber) +  ".png")