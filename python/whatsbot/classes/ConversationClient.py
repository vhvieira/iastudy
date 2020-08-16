import requests

"""
This class defines a whatsapp bot to be used in Phyton
TODO: Connect to conversation API
https://realpython.com/api-integration-in-python/
TODO: Analyze Dialogflow Python client libraries
"""
class ConversationClient:
    def __init__(self):
        print("Loading bot configuration")


    def sendMessageToBot(conversationId, messageContent):
        pass
    def sendEvent(conversationId, enventName):
        pass

task = {"summary": "Take out trash", "description": "" }
resp = requests.post('https://todolist.example.com/tasks/', json=task)
if resp.status_code != 201:
    raise ApiError('POST /tasks/ {}'.format(resp.status_code))
print('Created task. ID: {}'.format(resp.json()["id"]))