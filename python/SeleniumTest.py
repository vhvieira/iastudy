from selenium import webdriver
import time

"""
This class defines a whatsapp bot to be used in Phyton
"""
class WhatsappBot:
    def __init__(self):
        self.message = " Boa tarde Paulo, essa é uma messagem automatica!"
        self.to = ["Paulo Nunes"]
        self.options = webdriver.ChromeOptions()
        self.options.add_argument("lang=pt-br")
        self.options.add_argument("user-data-dir=temp");
        self.driver =  webdriver.Chrome(executable_path=r'./chromedriver.exe', options=self.options)

    #Method to send message
    #TODO: Read from a config file
    #TODO: Read new messages method
    #TODO: V1: How to read a file in Phyton (Java Endpoint will override the message files in the dir, phyton will read and send)
    #TODO: V2: Endpoint in Python then it directs modify the objects and not need for IO operations
    def SendMessages(self):
        print('Iniciando whatsapp robot')
        self.driver.get("https://web.whatsapp.com")
        time.sleep(10)
        #If we want to keep reading message, we can create a infinity loop here
        #Conversations are class div class="_210SC"
        for dest in self.to:
            print('Enviando mensagem para:' + dest)
            conversation = self.driver.find_element_by_xpath(f"//span[@title='{dest}']")
            time.sleep(3)
            print('Conversa encontrada')
            conversation.click()
            #[0] is search in the conversation, [1] is chatbox
            chatboxes = self.driver.find_elements_by_class_name("_3FRCZ")
            time.sleep(3)
            print('Chatboxs encontrados: ' + str(len(chatboxes)))
            chatboxes[1].click()
            chatboxes[1].send_keys(self.message)
            send_icon= self.driver.find_element_by_xpath("//span[@data-icon='send']")
            time.sleep(3)
            print('Send icon encontrado')
            send_icon.click()
            time.sleep(5)
            print('Mensagem enviada para:' + dest)

bot = WhatsappBot()
bot.SendMessages()