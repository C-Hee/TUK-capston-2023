import requests
import json
import urllib
from urllib.parse import urljoin
import time

class db_requester():
        def __init__(self, host):
                self.__host = host

        def request(self, path, param):
                url = urllib.parse.urljoin(self.__host, path)
                r = requests.post(url, json=param)
                return json.loads(r.text)

if __name__ == '__main__':
    db = db_requester('http://138.2.126.137:3004')
    #db = db_requester('http://127.0.0.1:3004')
    print(db.request('/request/day_from_month', {'id':'1', 'datetime':'2023-04-25 01:30:00'}))
    print(db.request('/request/week_from_month', {'id':'1', 'datetime':'2023-04-25 01:30:00'}))
    print(db.request('/request/month_from_year', {'id':'1', 'datetime':'2023-04-25 01:30:00'}))
    print(db.request('/update/gyro', {'id':'1', 'data':170}))
