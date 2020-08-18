class Test:
    def print5(self):
        print("005")

my_dict={'Dave' : '001' , 'Ava': '002' , 'Joe': '003'}
print(my_dict.keys())
print(my_dict.values())
print(my_dict.get('Dave'))
if my_dict.get('Yas') is not None:
    print('YES')
else:
    print('NO')
my_dict['Yas'] ='004' #key-value addition
print(my_dict.keys())
print(my_dict.values())
print(my_dict.get('Yas'))
#adding an object
obj = Test()
obj.print5()
dynamicName = 'Vic'
my_dict[dynamicName] = obj
my_dict.get(dynamicName).print5()
if my_dict.get(dynamicName) is not None:
    print('YES')
else:
    print('NO')