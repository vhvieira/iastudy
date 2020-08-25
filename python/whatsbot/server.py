import flask
from flask import request, jsonify
from classes.WhatsappBot import WhatsappBot
from classes.ConversationClient import ConversationClient
import time
import threading

#TODO Implement logging
#https://docs.python-guide.org/writing/logging/
#https://www.loggly.com/use-cases/6-python-logging-best-practices-you-should-be-aware-of/
app = flask.Flask("Whatsapp API")
bot = WhatsappBot()
listenersActive={'default' : True}
listenersList={'default' : {'1'}}
#thread config
delay = int(1)

@app.route('/', methods=['GET'])
def home():
    return "<h1>Welcome to whatsappbot server</h1>"

def thread_exec(myNumber, dests):
    while listenersActive.get(myNumber):
        bot.ReadMessagesFromList(myNumber, dests)
        time.sleep(delay)

def startThread(myNumber):
    if(listenersList.get(myNumber) is not None):
        listenersActive[myNumber]=True
        t1 = threading.Thread(target=thread_exec, args=(myNumber,listenersList.get(myNumber)))
        t1.start()

def stopThread(myNumber):
    if(listenersList.get(myNumber) is not None):
        listenersActive[myNumber]=False

@app.route('/sendMessage', methods=['POST'])
def sendMessage():
    #get request body
    data = request.get_json()
    myNumber = data["myNumber"]
    dest = data["destinatary"]
    text = data["text"]
    #stop thread if any
    stopThread(myNumber)
    #send message
    bot.SendMessage(myNumber, dest, text)
    #start thread if any
    startThread(myNumber)
    #return OK
    return jsonify(isError= False,
                    message= str(text),
                    statusCode= 200,
                    data= data), 200


@app.route('/sendEvent', methods=['POST'])
def sendEvent():
    #get request body
    data = request.get_json()
    myNumber = data["myNumber"]
    dest = data["destinatary"]
    eventName = data["event"]
    #stop thread if any
    stopThread(myNumber)
    #send message
    bot.SendEvent(myNumber, dest, eventName)
    #start thread if any
    startThread(myNumber)
    #send OK message
    return jsonify(isError= False,
                    message= str(eventName),
                    statusCode= 200,
                    data= data), 200

@app.route('/startListener', methods=['POST'])
def startListener():
    #get request body
    data = request.get_json()
    myNumber = data["myNumber"]
    dests = data["list"]
    #saving dest list
    listenersList[myNumber]=dests
    #call start thread
    startThread(myNumber)
    #return OK message
    return jsonify(isError= False,
                    message= "Listener started",
                    statusCode= 200,
                    data= data), 200

@app.route('/stopListener', methods=['POST'])
def stopListener():
    #get request body
    data = request.get_json()
    myNumber = data["myNumber"]
    #stop thread
    stopThread(myNumber)
    #return OK message
    return jsonify(isError= False,
                    message= "Listener stopped",
                    statusCode= 200,
                    data= data), 200

#default is http://localhost:5000/ 
app.run()