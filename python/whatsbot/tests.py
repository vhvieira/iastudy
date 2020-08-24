
#%% (Interactive Python runner in VSCode)
import datetime
from dateutil.relativedelta import relativedelta

myNumber='041920006463'
conversationIds={'default' : {'1'}}
twentyminsfromnow = datetime.datetime.now() + relativedelta(minutes=20)
conversationIds[myNumber]=twentyminsfromnow
date_now = datetime.datetime.now()
print(date_now > conversationIds[myNumber])
print(date_now < conversationIds[myNumber])
conversationId = myNumber + '_' + str(conversationIds[myNumber].strftime("%d%m%Y_%H%M%S"))
print(conversationId)
# %% (End of interative code)