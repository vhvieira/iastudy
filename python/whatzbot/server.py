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
        t1 = threading.Thread(target=thread_exec, args=(myNumber,listenersList.get(myNumber)))
        t1.start()

def stopThread(myNumber):
    factory.getLogger(myNumber).debug('Stoping listener thread for: ' + myNumber)
    if(listenersList.get(myNumber) is not None):
        listenersActive[myNumber]=False

@app.route('/sendMessage', methods=['POST'])
def sendMessage():
    #get request body
    data = request.get_json()
    factory.getLogger(defaultLogFile).debug('Calling sendMessage with data: ' + str(data))
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

#default is http://localhost:5000/ 
log = factory.getLogger(None)
log.debug("****** SERVER INITIALIZATION *******")
app.run(port=serverPort, debug=debugOption)