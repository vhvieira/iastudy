from classes.WhatsappBot import WhatsappBot
import sys

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
    #setup whatsapp web
    if command.__eq__("test"):
        bot.TestMessage()
		
else:
    print("This program requires an argument")
