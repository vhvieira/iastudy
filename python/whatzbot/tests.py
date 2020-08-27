
#%% (Interactive Python runner in VSCode)
from selenium import webdriver
import requests

FILE_SAVER_MIN_JS_URL = "https://raw.githubusercontent.com/eligrey/FileSaver.js/master/dist/FileSaver.min.js"

file_saver_min_js = requests.get(FILE_SAVER_MIN_JS_URL).content

chrome_options = webdriver.ChromeOptions()
driver = webdriver.Chrome('bin/chromedriver', options=chrome_options)

# Execute FileSaver.js in page's context
# driver.execute_script(file_saver_min_js)

# Now you can use saveAs() function
download_script = f''''
    return fetch("https://cdn.sstatic.net/Sites/stackoverflow/company/img/logos/so/so-logo.svg?v=a010291124bf",
        {{
            "credentials": "same-origin",
            "headers": {{"accept":"image/webp,image/apng,image/*,*/*;q=0.8","accept-language":"en-US,en;q=0.9"}},
            "referrerPolicy": "no-referrer-when-downgrade",
            "body": null,
            "method": "GET",
            "mode": "cors"
        }}
    ).then(resp => {{
        return resp.blob();
    }}).then(blob => {{
        saveAs(blob, 'stackoverflow_logo.svg');
    }});
    '''
#script = 'var file = new File(["Hello, world!"], "hello world.txt", {type: "text/plain;charset=utf-8"});saveAs(file);'
script = 'saveAs("blob:https://upload.wikimedia.org/wikipedia/commons/c/c8/Example.ogg", "test.ogg");'
#script2 = 'var blob = new Blob([save], {type: "text/plain;charset=utf-8"});'
#driver.execute_script(script.strip().decode())
driver.execute_script(file_saver_min_js.strip().decode())
#driver.execute_script(download_script.strip())
driver.execute_script(script.strip())
#driver.execute_script(script2.strip())
# Done! Your browser has saved an SVG image!

# %% (End of interative code)