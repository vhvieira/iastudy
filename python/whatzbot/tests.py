
#%% (Interactive Python runner in VSCode)
import datetime
date_formated=datetime.datetime.now().strftime("%m/%d/%Y")
print(date_formated)
simple_date = '{dt.month}/{dt.day}/{dt.year}'.format(dt=datetime.datetime.now())
new_date = '{dt:%A} {dt:%B} {dt.day}, {dt.year}'.format(dt=datetime.datetime.now())
print(simple_date)
print(new_date)
# %% (End of interative code)