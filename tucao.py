# -*- coding:utf-8 -*-
import urllib2
from bs4 import BeautifulSoup

for pagenum in range(1,5):
    f = open('Tucao.txt','a')
    strpagenum = str(pagenum)
    #url = "http://in.ali213.net/news/ " + strpagenum + ".html"
    url = "https://www.zhihu.com/collection/27109279?page=" + strpagenum
    page = urllib2.urlopen(url)#打开网址
    content = page.read()#读取所有
    #print content
    soup = BeautifulSoup(content)#创建一个对象
    #All = soup.findAll(attrs={'class': ['ltl-l-o-r-t', 'ltl-l-o-r-m']})
    All = soup.findAll(attrs = {'class' : ['zm-item-title','zh-summary']})
    #print All #findAll 从源码中找到标签所在的内容
    for each in All:#打印话题+回复内容
        try:
            if each.name == 'h2':#h2代表的是话题
                print each.a.string
                if each.a.string:#获取标签里的文字
                    f.write('问题：'+each.a.string+'\n')
                else:
                    f.write("无内容")
            else:#代表回复的内容
                print each.get_text()+'\n'
                if each.get_text():

                    content = each.get_text().replace('显示全部内容')

                    f.write('回复: '+content.strip()+'\n\n')#strip()删除括号内的东西
                else:
                    f.write("无内容")
        except Exception as e:
            continue
    f.close()