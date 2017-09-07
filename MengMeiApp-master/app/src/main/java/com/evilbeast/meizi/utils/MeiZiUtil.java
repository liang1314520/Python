package com.evilbeast.meizi.utils;

import com.evilbeast.meizi.entity.photo.PhotoGroupObject;
import com.evilbeast.meizi.entity.photo.PhotoObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Author: sumary
 */
public class MeiZiUtil {
    private static volatile MeiZiUtil mInstance;
    private MeiZiUtil() {}

    // 单例模式
    public static MeiZiUtil getInstance() {
        if (mInstance == null) {
            synchronized (MeiZiUtil.class) {
                if (mInstance == null) {
                    mInstance = new MeiZiUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 解析妹子图html
     *
     * @param html
     * @param type
     * @return
     */
    public List<PhotoGroupObject> parserMeiziTuHtml(String html, String type, String module)
    {

        List<PhotoGroupObject> list = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements links = doc.select("li");

        Element aelement;
        Element imgelement;
        for (int i = 7; i < links.size(); i++)
        {
            imgelement = links.get(i).select("img").first();
            aelement = links.get(i).select("a").first();
            String url = aelement.attr("href");
            PhotoGroupObject bean = new PhotoGroupObject();
            bean.setPosition(i);
            bean.setTitle(imgelement.attr("alt").toString());
            bean.setType(type);
            bean.setImageUrl(imgelement.attr("data-original"));
            bean.setGroupId(url2groupid(url));
            bean.setModule(module);
            list.add(bean);
        }
        return list;
    }

    /**
     * 获取妹子图的GroupId
     *
     * @param url
     * @return
     */
    private int url2groupid(String url)
    {

        return Integer.parseInt(url.split("/")[3]);
    }

    public void putMeiZiCache(List<PhotoGroupObject> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealm(list);
        realm.commitTransaction();
        realm.close();
    }

    public void putGroupCache(List<PhotoObject> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(list);
        realm.commitTransaction();
        realm.close();
    }

    public List<PhotoObject> parseMeiZiGroupSave(String html, int groupId, String module) {
        List<PhotoObject> results = new ArrayList<>();
        String keyNum;
        String imageUrl;
        String keyPrefix;
        String keyExt;

        Document doc = Jsoup.parse(html);
        String url = doc.select("div.main-image img").first().attr("src");
        Elements spanElements = doc.select("div.pagenavi a span");
        int total_page = Integer.parseInt(spanElements.get(spanElements.size()-2).text());
        LogUtil.all(total_page+"");
        String urlPrefix = url.substring(0, url.lastIndexOf("/"));
        String pathName = url.substring(url.lastIndexOf("/")+1);
        if (pathName.lastIndexOf("01.") != -1) {
            keyPrefix = pathName.substring(0, pathName.lastIndexOf("01."));
            keyExt = pathName.substring(pathName.lastIndexOf("."));
        } else {
            keyPrefix = null;
            keyExt = null;
        }


        for (int i = 1; i <= total_page; i++) {
            if (keyPrefix != null) {
                keyNum = String.format("%2d", i).replace(' ', '0');
                imageUrl = String.format("%s/%s%s%s", urlPrefix, keyPrefix, keyNum, keyExt);
            } else {
                imageUrl = "";
            }

            PhotoObject data = new PhotoObject();
            data.setGroupId(groupId);
            data.setModule(module);
            data.setImageUrl(imageUrl);
            data.setPosition(i);
            results.add(data);
        }
        return results;
    }

    public String parseMeiziImageUrl(String html) {
        Document doc = Jsoup.parse(html);
        return doc.select("div.main-image img").first().attr("src");
    }

    public List<PhotoGroupObject> parseFuliItems(String html, String module) {
        List<PhotoGroupObject> results = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements aTags = doc.select("div.movie-item > a");
        LogUtil.all(aTags.size()+"#");
        for (int i = 0; i < aTags.size(); i++) {
            Element aTag = aTags.get(i);
            String title = aTag.attr("title");

            String href = aTag.attr("href");
            int groupId = Integer.parseInt(href.substring(href.lastIndexOf("/")+1, href.lastIndexOf(".")));
            String imageUrl = aTag.select("img").first().attr("src");

            PhotoGroupObject item = new PhotoGroupObject();
            item.setImageUrl(imageUrl);
            item.setGroupId(groupId);
            item.setTitle(title);
            item.setModule(module);

            results.add(item);
        }


        LogUtil.all(results.toString());
        return results;
    }

    public void putFuliItemCache(final List<PhotoGroupObject> list) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(list);
            }
        });
    }

    public List<PhotoObject> parseFuliGroupHtml(String html,int groupId, String module) {
        List<PhotoObject> results = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        String href = doc.select("ul.dslist-group li a").last().attr("href");
        String src = doc.select("div.playpic img").first().attr("src");
        String srcPre = src.substring(0, src.lastIndexOf("/"));
        String srcExt = src.substring(src.lastIndexOf(".")+1);
        int num = Integer.parseInt(href.substring(href.lastIndexOf("/")+1, href.lastIndexOf(".")));
        if (num > 0) {
            for (int i = 1; i <= num; i++) {
                String imageUrl = String.format("%s/%d.%s", srcPre, i, srcExt);
                PhotoObject item = new PhotoObject();
                item.setModule(module);
                item.setGroupId(groupId);
                item.setImageUrl(imageUrl);
                item.setPosition(i);
                results.add(item);
            }
        }
        return results;
    }

    public String parseFuliImageUrl(String html) {
        Document doc = Jsoup.parse(html);
        return  doc.select("div.playpic img").first().attr("src");
    }
}
