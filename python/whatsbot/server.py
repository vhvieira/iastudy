import flask
from flask import request, jsonify
from classes.WhatsappBot import WhatsappBot
from classes.ConversationClient import ConversationClient
import time

app = flask.Flask("Whatsapp API")
app.config["DEBUG"] = True
bot = WhatsappBot()
listenersActive={'default' : True}

@app.route('/', methods=['GET'])
def home():
    return "<h1>Welcome to whatsappbot server</h1>"

#TODO: Send message stop listener and then restart after
@app.route('/sendMessage', methods=['POST'])
def sendMessage():
    data = request.get_json()
    myNumber = data["myNumber"]
    dest = data["destinatary"]
    text = data["text"]
    bot.SendMessage(myNumber, dest, text)
    return jsonify(isError= False,
                    message= str(text),
                    statusCode= 200,
                    data= data), 200

#TODO: Send event stop listener and then restart after
@app.route('/sendEvent', methods=['POST'])
def sendEvent():
    data = request.get_json()
    myNumber = data["myNumber"]
    dest = data["destinatary"]
    eventName = data["event"]
    bot.SendEvent(myNumber, dest, eventName)
    return jsonify(isError= False,
                    message= str(eventName),
                    statusCode= 200,
                    data= data), 200

@app.route('/startListener', methods=['POST'])
def startListener():
    data = request.get_json()
    myNumber = data["myNumber"]
    dests = data["list"]
    listenersActive[myNumber]=True
    #TODO: Create a new thread here, make sleep configurable
    while listenersActive.get(myNumber):
        bot.ReadMessagesFromList(myNumber, dests)
        time.sleep(1)
    return jsonify(isError= False,
                    message= "Listener started",
                    statusCode= 200,
                    data= data), 200

@app.route('/stopListener', methods=['POST'])
def stopListener():
    data = request.get_json()
    myNumber = data["myNumber"]
    listenersActive[myNumber]=False
    return jsonify(isError= False,
                    message= "Listener stopped",
                    statusCode= 200,
                    data= data), 200
#default is http://localhost:5000/ 
app.run()