# -*- coding:utf-8 -*-
from Tkinter import *
import tkMessageBox
import urllib
import json
import mp3play
import time
import threading
import random
def music():
    text = music_input.get()#判断搜索框的内容
    if not text:#如果没有输入内容
        tkMessageBox.showinfo('温馨提示','请先输入歌曲名或歌手名，再进行搜索')
        return
    html = urllib.urlopen('http://s.music.163.com/search/get/?type=1&s=%E6%B5%B7%E9%98%94%E5%A4%A9%E7%A9%BA&limit=9').read()#读取网址内容
    text = json.loads(html)#转换成字典
    music_list = text['result']['songs']#将需要的内容保存起来
    global mp3_list
    mp3_list = []#获取歌曲的url
    music_show.delete(0,music_show.size())#删除之前的列表
    for i in music_list:#取出其中每个内容
        music_show.insert(0,i['name'] + '(' + i['artists'][0]['name'] + ')')#音乐展示区插入网址中保存的内容
        mp3_list.append(i['audio'])#插入歌曲的url

def music_click():#双击触发事件
    index = music_show.curselection()[0]#获取歌曲索引
    filename = r'%s.mp3'%random.randint(1000,9999)
    urllib.urlretrieve(mp3_list[index],filename)#下载歌曲
    mp3 = mp3play.load(filename=filename)#文件名
    mp3.play()#播放
    time.sleep(mp3.seconds())# sleep()延时 mp3.second()歌曲的总时长
    mp3.stop()
def th(event):#多线程
    thr = threading.Thread(target=music_click())
    thr.start()#新建一个线程

#窗口设置
root = Tk()#实例化窗口对象，创建窗口
root.title('Music 播放器')#更改标题
root.geometry('+350+80')#窗口大小 不指定大小，会根据控件大小自动调整
#放置控件，每个控件看成一个单独的窗口
music_input = Entry(root,width=60)#单行输入框，创建一个输入框（布局）
music_input.pack()#布局方式 grid（）
music_serch = Button(root,text='搜 索',command=music)#绑定点击事件
music_serch.pack()
var = StringVar()#定义 var
music_show = Listbox(root,width=100,height=30,listvariable = var)
music_show.bind('<Double-Button-1>',th)#绑定事件（触发点）
music_show.pack()
music_label = Label(root,text='欢迎使用音乐播放器',fg='orange')
music_label.pack()
mainloop()#显示窗口，结束