
#%% (Interactive Python runner in VSCode)
import datetime
from dateutil.relativedelta import relativedelta

#TODO: use this for 20 mins conversation id
one_year_from_now = datetime.datetime.now() + relativedelta(years=1)
date_formated = datetime.datetime.now().strftime("%d/%m/%Y")
print(date_formated)
# %% (End of interative code)