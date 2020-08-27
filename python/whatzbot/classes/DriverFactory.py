from selenium import webdriver
from classes.Configuration import Configuration
from selenium.webdriver.chrome.options import Options
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
        self.fileStorage=config.getConfigValue("fileStorage")

    # Create a new browser driver
    # TODO: How to make it headless link:
    # https://duo.com/decipher/driving-headless-chrome-with-python
    # https://developers.google.com/web/updates/2017/04/headless-chrome
    def createNew(self, myNumber, loadSleep):
        #self.options = webdriver.ChromeOptions()
        self.options = Options()
        #self.options.binary_location = r'C:\Users\victor\AppData\Local\Google\Chrome SxS\Application\chrome.exe'
        #self.options.add_argument('--headless')
        self.options.add_argument('--disable-gpu')  # Last I checked this was necessary.
        self.options.add_argument('--no-sandbox') # Bypass OS security model
        self.options.add_argument("--disable-notifications")
        self.options.add_argument("--disable-extensions")
        self.options.add_argument('--verbose')
        self.options.add_argument('disable-infobars')
        self.options.add_experimental_option("prefs", {
                "download.default_directory": self.fileStorage,
                "download.prompt_for_download": False,
                "download.directory_upgrade": True,
                "safebrowsing_for_trusted_sources_enabled": False,
                "safebrowsing.enabled": False
        })
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