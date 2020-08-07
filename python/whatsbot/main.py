import sys
from classes.WhatsappBot import WhatsappBot
from classes.HttpServer import SimpleHTTPRequestHandler

#TODO: Read new messages method
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


