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
        self.options.add_argument("user-data-dir=c:\\Users\\vhrodriguesv\\AppData\\Local\\Google\\Chrome\\User");
        self.driver =  webdriver.Chrome(executable_path=r'./chromedriver.exe', options=self.options)

    #Method to send message
    def SendMessages(self):
        print('Iniciando whatsapp robot')
        self.driver.get("https://web.whatsapp.com")
        time.sleep(30)
        #If we want to keep reading message, we can create a infinity loop here
        for dest in self.to:
            self.driver.find_element_by_xpath(f"//span[@title'{dest}']")
            time.sleep(3)
            chatbox = self.driver.find_element_by_class_name("_13mgZ")
            time.sleep(3)
            chatbox.click()
            chatbox.send_keys(self.message)
            send_button = self.driver.find_element_by_xpath("//span[@data-icon='send']")
            time.sleep(3)
            send_button.click()
            time.sleep(5)

bot = WhatsappBot()
bot.SendMessages()