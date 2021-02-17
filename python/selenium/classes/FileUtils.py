from classes.Configuration import Configuration
from classes.LoggerFactory import LoggerFactory
from classes.DriverFactory import DriverFactory
from pathlib import Path
import requests
import datetime

"""
This class it is a utility class for Files operations in python
"""
class FileUtils:
    #initializing configuration
    def __init__(self):
        config = Configuration()
        self.urlCleanupStr=config.getConfigValue("urlCleanupStr").split(';')
        self.fileStorage=config.getConfigValue("fileStorage")
        self.conversationDateFormat=config.getConfigValue("conversationDateFormat")
        self.audioFilesExtension=config.getConfigValue("audioFilesExtension")
        self.loggerFactory = LoggerFactory()
        self.driverFactory=DriverFactory()

    #list all files in a directory
    def listFiles(self, myNumber, dirPath):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            p = Path(dirPath)
            return list(p.glob('*'))
        except Exception as ex:
            log.error('Error when try to list files for directory: '  + dirPath, ex)
            return None

    #read file from path
    def readFile(self, myNumber, filePath):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            messageFile = Path(filePath)
            return str(messageFile.read_text())
        except Exception as ex:
            log.error('Error when try to read file in: '  + filePath, ex)
            return None

    #write a file to a path
    def writeFile(self, myNumber, filePath, fileContent):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            log.debug('Received filePath: ' + filePath)
            log.debug('Received fileContent: ' + str(fileContent))
            #Simple Writing and Reading content
            p = Path(filePath)
            #if(isinstance(fileContent, str)):
            #    p.write_text(fileContent)
            #else:
            p.write_bytes(fileContent)
        except Exception as ex:
            log.error('Error when try to save file in: '  + filePath, ex)

    #FileSaver download
    def downloadWithFileSaver(self, myNumber, driver, url, fileName):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            FILE_SAVER_MIN_JS_URL = "https://raw.githubusercontent.com/eligrey/FileSaver.js/master/dist/FileSaver.min.js"
            file_saver_min_js = requests.get(FILE_SAVER_MIN_JS_URL).content
            # Now you can use saveAs() function
            download_script = 'saveAs("{url}", "{fileName}");'
            download_script = download_script.replace("{url}", url).replace("{fileName}", fileName + self.audioFilesExtension)
            # Execute FileSaver.js in page's context
            driver.execute_script(file_saver_min_js.strip().decode())
            log.debug('Will execute script: ' + download_script)
            driver.execute_script(download_script.strip())
        except Exception as ex:
            log.error('Error when try to download file with FileSaver(downloadWithFileSaver) from: '  + url, ex)

    #download as file and save in local storage
    def downloadFileAs(self, myNumber, url, fileName):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            #prepare url
            for cleanStr in self.urlCleanupStr:
                url = url.replace(cleanStr, '')
            log.debug('Will download file from url: ' + url)
            r = requests.get(url, allow_redirects=True)
            log.debug('Will save file in path: ' + self.fileStorage + fileName)
            self.writeFile(myNumber, self.fileStorage + fileName, r.content)
        except Exception as ex:
            log.error('Error when try to download file from: '  + url, ex)
    
    #download a file using driver
    def downloadFileWithDriver(self, myNumber, dest, url, driver):
        log = self.loggerFactory.getLogger(myNumber)
        try:
            #prepare url
            for cleanStr in self.urlCleanupStr:
                url = url.replace(cleanStr, '')
            log.debug('Will download file from url: ' + url)
            fileName = dest + '_' + str(datetime.datetime.now().strftime(self.conversationDateFormat))
            self.downloadWithFileSaver(myNumber, driver, url, fileName)
            return fileName
        except Exception as ex:
            log.error('Error when try to download file from: '  + url, ex)
            return None
    