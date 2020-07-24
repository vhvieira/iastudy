Install:
Extract the zip file in any folder.

Download and install Python 3.8 or high
(https://www.python.org/downloads/release/python-380/)

Install Python packages with the commands:
pip install selenium
pip install PyYAML

Using:
1) In config folder edit the file list.txt for adding the name of people you want to sent the  message, it must be the name as in your phone's agenda. Use ';' to separate the names.

2) Edit message.txt to edit the message you want to send.

3) Also edit test.txt and testMessage.txt to configure the test message service.

4) Run setup.bat to scan the whatsapp web QR code

5) Run test.bat to send the test message

6) Run sendmessagens.bat to send the automatic messages

Notes: If the program stops working it means whatsapp web changed some of it is HTML elements, so you can manually enter the web.whatapps.com and inspect the elements and fix the configuration of this program in file config/config.yml