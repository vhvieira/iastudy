import sys
from classes.WhatsappBot import WhatsappBot
from classes.ConversationClient import ConversationClient
import time

#main method (main execution)
print ('Number of arguments:', len(sys.argv), 'arguments.')
print ('Argument List:', str(sys.argv))

#checking python argumens, first is the main.py, second is the command
#sys.argv[1] -> Action
#sys.argv[2] -> Wait time in seconds
#sys.argv[3] -> The number/profile_id
if (len(sys.argv) > 2):
    command = str(sys.argv[1])
    print('Iniciando whatsapp robot')
    bot = WhatsappBot()
	#send message
    if command.__eq__("send"):
        bot.SendBatchMessages(sys.argv[2])
    #setup whatsapp web
    if command.__eq__("setup"):
        bot.Setup(sys.argv[2], sys.argv[3], sys.argv[4])
    #test send api (integration with google)
    if command.__eq__("test"):
        client = ConversationClient()
        response = client.sendSimpleMessage(sys.argv[2])
        print("Response: " + response)
    if command.__eq__("testEvt"):
        client = ConversationClient()
        response = client.sendSimpleEvent(sys.argv[2])
        print("Response: " + response)
    if command.__eq__("read"):
       bot.ReadNewMessages(sys.argv[2])
       time.sleep(10)
       bot.ReadNewMessages(sys.argv[2])
    if command.__eq__("hi"):
       bot.Test(sys.argv[2])
else:
    print("This program requires at least three argument. See documentation!")