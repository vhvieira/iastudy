import sys
import json
from http.server import HTTPServer, BaseHTTPRequestHandler
from threading import Thread
from classes.WhatsappBot import WhatsappBot

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