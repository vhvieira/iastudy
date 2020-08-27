from selenium import webdriver
from classes.Configuration import Configuration
import time

"""
This class is the selenium driver factory
"""
class DriverFactory:
    def __init__(self):
        config = Configuration()
        self.my_drivers={'default' : '001'}
        self.lang=config.getConfigValue("lang")
        self.data_dir=config.getConfigValue("data_dir")
        self.url=config.getConfigValue("whatsapp_url")

    def createNew(self, myNumber, loadSleep):
        self.options = webdriver.ChromeOptions()
        self.options.add_argument("lang=" + self.lang)
        self.options.add_argument("user-data-dir=" + self.data_dir + str(myNumber))
        self.driver =  webdriver.Chrome(executable_path=r'./bin/chromedriver.exe', options=self.options) 
        self.my_drivers[myNumber] = self.driver
        self.driver.get(self.url)
        time.sleep(loadSleep)
        return self.driver

    def getDriver(self, myNumber, loadSleep):
        if self.my_drivers.get(myNumber) is not None:
            return self.my_drivers.get(myNumber)
        else:
            return self.createNew(myNumber, loadSleep)