from classes.WhatsappBot import WhatsappBot
import sys
from http.server import HTTPServer, BaseHTTPRequestHandler
import json
from threading import Thread

class WhatsThread(Thread):
    def __init__ (self, bot, message):
        Thread.__init__(self)
        self.message = message
        self.bot = bot

    def run(self):
        obj = json.loads(self.message)
        print(obj['participants'])
        print(obj['text'])
        self.bot.SendMessages(obj['participants'], obj['text'])
                      
class SimpleHTTPRequestHandler(BaseHTTPRequestHandler):
    bot = WhatsappBot()
    def do_POST(self):
        self.send_response(200)
        self.end_headers()
        self.wfile.write(b'OK')
        content_len = int(self.headers.get('Content-Length'))
        post_body = self.rfile.read(content_len)
        whats_thread = WhatsThread(bot, post_body)
        whats_thread.start()

def run(server_class=HTTPServer, handler_class=BaseHTTPRequestHandler):
    server_address = ('0.0.0.0', 8000)
    httpd = server_class(server_address, handler_class)
    print('Starting Server in the port 8000')
    httpd.serve_forever()

#TODO: Read new messages method
#TODO: V1: How to read a file in Phyton (Java Endpoint will override the message files in the dir, phyton will read and send)
#TODO: V2: Endpoint in Python then it directs modify the objects and not need for IO operations
#main method (main execution)
print ('Number of arguments:', len(sys.argv), 'arguments.')
print ('Argument List:', str(sys.argv))

#checking python argumens, first is the main.py, second is the command
if (len(sys.argv) > 1):
    command = str(sys.argv[1])
    print('Iniciando whatsapp robot')
    bot = WhatsappBot()
	#send message
    if command.__eq__("send"):
        bot.SendMessages()
    #setup whatsapp web
    if command.__eq__("setup"):
        bot.Setup()
    #test whatsapp web
    if command.__eq__("test"):
        bot.TestMessage()
    #read whatsapp web
    if command.__eq__("read"):
        bot.ReadMessages()
    if command.__eq__("server"):
        run(handler_class=SimpleHTTPRequestHandler)
		
else:
    print("This program requires an argument")


