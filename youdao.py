from tkinter import *
import urllib.request,urllib.parse
import json
def translate(event=None):
    content = input_text.get(1.0,END)
    show_text.delete(1.0,END)
    url = 'http://fanyi.youdao.com/translate?smartresult=dict&smartresult=rule&smartresult=ugc&sessionFrom=https://www.baidu.com/link'
    data ={'type':'AUTO',
      'i':content,
      'doctype':'json',
      'xmlVersion':1.8,
      'keyfrom':'fanyi.web',
      'ue':'UTF-8',
      'action':'FY_BY_ENTER',
      'typoResult':'true'}
    data = urllib.parse.urlencode(data).encode('utf-8')
    response = urllib.request.urlopen(url,data)
    html = response.read().decode('utf-8')
    target = json.loads(html)
    target = target['translateResult'][0][0]['tgt']
    show_text.insert(END,target)
    #translate_button = target['translateResult'][0][0]['tgt']
     

root = Tk()
root.geometry('700x550+300+100')
Label(root,text='有道翻译',fg='skyblue',font=('roman',15,'bold')).pack()
# text=    fg=   系统自带,名字不能更改
input_text = Text(root,width=100,height=10,font=('roman',15,'bold'))
show_text = Text(root,width=100,height=10,font=('roman',15,'bold'))
translate_button = Button(root,text='翻译',command=translate)
input_text.bind('RETURN')
input_text.pack()
show_text.pack()
translate_button.pack()
mainloop()
