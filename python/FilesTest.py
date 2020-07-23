from pathlib import Path

#Virtual Paths
myFiles = ['accounts.txt', 'details.csv', 'invite.docx']
for filename in myFiles:
   print(Path(r'C:\Users\Al', filename))

#Listing existing files
p = Path('D:/DEV/git/iastudy/python')
fileList = list(p.glob('*'))

for filename in fileList:
   print(filename)

#Simple Writing and Reading content
p = Path('spam.txt')
p.write_text('Hello, world!')
print( 'content: ' + str(p.read_text()) )