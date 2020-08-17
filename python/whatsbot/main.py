import sys
from classes.WhatsappBot import WhatsappBot
from classes.ConversationClient import ConversationClient

#main method (main execution)
print ('Number of arguments:', len(sys.argv), 'arguments.')
print ('Argument List:', str(sys.argv))

#checking python argumens, first is the main.py, second is the command
#sys.argv[1] -> Action
#sys.argv[2] -> Wait time in seconds
#sys.argv[3] -> The number/profile_id
if (len(sys.argv) > 3):
    command = str(sys.argv[1])
    print('Iniciando whatsapp robot')
    bot = WhatsappBot()
	#send message
    if command.__eq__("send"):
        bot.SendMessages(sys.argv[2], sys.argv[3])
    #setup whatsapp web
    if command.__eq__("setup"):
        bot.Setup(sys.argv[2], sys.argv[3], sys.argv[4])
    #test send api (integration with google)
    if command.__eq__("test"):
        client = ConversationClient()
        response = client.sendSimpleMessage(sys.argv[3])
        print("Response: " + response)
    if command.__eq__("testEvt"):
        client = ConversationClient()
        response = client.sendSimpleEvent(sys.argv[3])
        print("Response: " + response)
else:
    print("This program requires at least three argument. See documentation!")