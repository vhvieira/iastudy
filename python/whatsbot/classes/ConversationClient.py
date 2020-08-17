import requests
import json
from classes.Configuration import Configuration
from requests.auth import HTTPBasicAuth

"""
This class is a client to conversation-api endpoint
"""
class ConversationClient:
    def __init__(self):
        print("Loading client configuration")
        config = Configuration()
        self.baseURL = config.getConfigValue("baseURL")
        self.apiVersion = config.getConfigValue("apiVersion")
        self.apiUser = config.getConfigValue("apiUser")
        self.apiPass = config.getConfigValue("apiPass")
        self.defaultLanguage = config.getConfigValue("defaultLanguage")
        self.httpHeaders = config.getConfigValue("httpHeaders")
        self.sendMessageURL = config.getConfigValue("sendMessageURL").replace("{baseURL}", self.baseURL).replace("{version}", self.apiVersion)
        self.sendEventURL = config.getConfigValue("sendEventURL").replace("{baseURL}", self.baseURL).replace("{version}", self.apiVersion)
        self.simpleMessageTemplate = config.getConfigValue("simpleMessageTemplate")
        self.continuousMessageTemplate = config.getConfigValue("continuousMessageTemplate")
        self.simpleEventTemplate = config.getConfigValue("simpleEventTemplate")
        self.continuousEventTemplate = config.getConfigValue("continuousEventTemplate")

    def sendSimpleMessage(self, messageContent):
        requestJson = self.simpleMessageTemplate.replace("{text}", messageContent).replace("{language}", self.defaultLanguage)
        return self.processPostRequest(self.sendMessageURL, requestJson)
    def sendSimpleMessageWithLang(self, messageContent, language):
        requestJson = self.simpleMessageTemplate.replace("{text}", messageContent).replace("{language}", language)
        return self.processPostRequest(self.sendMessageURL, requestJson)
    def sendContinuousMessage(self, conversationId, messageContent):
        requestJson = self.continuousMessageTemplate.replace("{conversationId}", conversationId).replace("{text}", messageContent).replace("{language}", self.defaultLanguage)
        return self.processPostRequest(self.sendMessageURL, requestJson)
    def sendContinuousMessageWithLang(self, conversationId, messageContent, language):
        requestJson = self.continuousMessageTemplate.replace("{conversationId}", conversationId).replace("{text}", messageContent).replace("{language}", language)
        return self.processPostRequest(self.sendMessageURL, requestJson)

    def sendSimpleEvent(self, eventName):
        requestJson = self.simpleEventTemplate.replace("{event}", eventName).replace("{language}", self.defaultLanguage)
        return self.processPostRequest(self.sendEventURL, requestJson)

    def sendSimpleEventWithLang(self, eventName, language):
        requestJson = self.simpleEventTemplate.replace("{event}", eventName).replace("{language}", language)
        return self.processPostRequest(self.sendEventURL, requestJson)

    def sendContinuosEvent(self, conversationId, eventName):
        requestJson = self.continuousEventTemplate.replace("{conversationId}", conversationId).replace("{event}", eventName).replace("{language}", self.defaultLanguage)
        return self.processPostRequest(self.sendEventURL, requestJson)

    def sendContinuosEventWithLang(self, conversationId, eventName, language):
        requestJson = self.continuousEventTemplate.replace("{conversationId}", conversationId).replace("{event}", eventName).replace("{language}", language)
        return self.processPostRequest(self.sendEventURL, requestJson)

    """
    Method that executes the post request using requests lib
    """
    def processPostRequest(self, endpointURL, requestJson):
        print("Will send the request: " + str(requestJson))
        print("URL used is: " + str(endpointURL))
        resp = requests.post(endpointURL, auth=HTTPBasicAuth(self.apiUser, self.apiPass), json= eval(requestJson), headers=eval(self.httpHeaders))
        print("Got the response: " + str(resp))
        if resp.status_code != 200:
          print('ERROR on calling method {}'.format(resp.status_code))
        #print('Created task. ID: {}'.format(resp.json()["id"]))
        print('Response JSON {}'.format(resp.json()))
        return resp.json()["responseText"]