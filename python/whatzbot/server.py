import flask
from flask import request, jsonify
from classes.WhatsappBot import WhatsappBot
from classes.Configuration import Configuration
from classes.LoggerFactory import LoggerFactory
import time
import sys
import threading

#Log factory implementation
#https://docs.python-guide.org/writing/logging/
#https://www.loggly.com/use-cases/6-python-logging-best-practices-you-should-be-aware-of/
factory = LoggerFactory()
serverPort = 5000 #default server port
debugOption = True #Debug is enable by default

#if arguments are provided then read custom config
if (len(sys.argv) > 2):
    #arg 1 is env name (to get custom properties)
    if(sys.argv[1] is not None): 
        configFile = "config/config-" + sys.argv[1] + ".yml"
        logConfigFile = "config/logconfig-" + sys.argv[1] + ".ini"
        config = Configuration()
        config.loadCustomFile(configFile)
        factory.loadCustomFile(logConfigFile)

    #arg[2] is server port (default is 5000)
    if(sys.argv[2] is not None): 
        serverPort = int(sys.argv[2])
    #arg[3] is debug option (default is false) - If present is true
    if(len(sys.argv) > 3 and sys.argv[3] is not None): 
        debugOption = False

#thread delay config
delay = int(1)

#internal server objects
app = flask.Flask("Whatsapp API")
bot = WhatsappBot()
listenersActive={'default' : True}
listenersList={'default' : {'1'}}

@app.route('/', methods=['GET'])
def home():
    return "<h1>Welcome to whatsappbot server</h1>"

def thread_exec(myNumber, dests):
    while listenersActive.get(myNumber):
        bot.ReadMessagesFromList(myNumber, dests)
        time.sleep(delay)

def startThread(myNumber):
    factory.getLogger(myNumber).debug('Starting listener thread for: ' + myNumber)
    if(listenersList.get(myNumber) is not None):
        listenersActive[myNumber]=True
        #loading audio bufffer
        bot.LoadLatestAudioFromList(myNumber, listenersList.get(myNumber))
        #starting read messasges
        t1 = threading.Thread(target=thread_exec, args=(myNumber,listenersList.get(myNumber)))
        t1.start()

def stopThread(myNumber):
    factory.getLogger(myNumber).debug('Stoping listener thread for: ' + myNumber)
    if(listenersList.get(myNumber) is not None):
        listenersActive[myNumber]=False
        #unload from audio buffer
        bot.UnloadLatestAudioFromList(myNumber, listenersList.get(myNumber))

#Simple method that sends a message to whatsapp number
@app.route('/sendMessage', methods=['POST'])
def sendMessage():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Calling sendMessage with data: ' + str(data))
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

#Method that triggers a event in conversation-api and sends it to the whatsapp number
@app.route('/sendEvent', methods=['POST'])
def sendEvent():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Calling sendEvent with data: ' + str(data))
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

#start listener for new messages, integrated with conversation-api
@app.route('/startListener', methods=['POST'])
def startListener():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Calling startListener with data: ' + str(data))
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
                    
#stop listener for new messages
@app.route('/stopListener', methods=['POST'])
def stopListener():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Calling stopListener with data: ' + str(data))
    myNumber = data["myNumber"]
    #stop thread
    stopThread(myNumber)
    #return OK message
    return jsonify(isError= False,
                    message= "Listener stopped",
                    statusCode= 200,
                    data= data), 200

#add a new number for a active listener, integrated with conversation-api
@app.route('/addNumber', methods=['POST'])
def addNumberToListener():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Adding number with data: ' + str(data))
    myNumber = data["myNumber"]
    dest = data["dest"]
    #stop thread if any
    stopThread(myNumber)
    #adding new number
    listenersList.get(myNumber).append(dest)
    #start thread if any
    startThread(myNumber)
    #return OK message
    return jsonify(isError= False,
                    message= "Number added",
                    statusCode= 200,
                    data= data), 200

#remove a number for a active listener, integrated with conversation-api
@app.route('/removeNumber', methods=['POST'])
def removeNumberFromListener():
    #get request body
    data = request.get_json()
    factory.getLogger(None).debug('Adding number with data: ' + str(data))
    myNumber = data["myNumber"]
    dest = data["dest"]
    #stop thread if any
    stopThread(myNumber)
    #remove number from listening list
    listenersList.get(myNumber).remove(dest)
    #start thread if any
    startThread(myNumber)
    #return OK message
    return jsonify(isError= False,
                    message= "Number removed from listening",
                    statusCode= 200,
                    data= data), 200
                    
#initializing local server                    
#default is http://localhost:5000/ 
log = factory.getLogger(None)
log.debug("****** SERVER INITIALIZATION *******")
app.run(port=serverPort, debug=debugOption)