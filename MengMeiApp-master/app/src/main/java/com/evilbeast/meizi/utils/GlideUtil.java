package com.evilbeast.meizi.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import retrofit2.http.Path;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Author: sumary
 */
public class GlideUtil {

    public static Observable<Uri> saveImageToLocal(final Context context, final String url)
    {
        return Observable.create(new Observable.OnSubscribe<File>()
        {

            @Override
            public void call(Subscriber<? super File> subscriber)
            {

                File file = null;
                try
                {
                    LogUtil.all("download" + url);
                    file = Glide.with(context)
                            .load(url)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                    subscriber.onNext(file);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
        }).flatMap(new Func1<File,Observable<Uri>>()
        {

            @Override
            public Observable<Uri> call(File file)
            {

                File mFile = null;
                try
                {

                    String path = Environment.getExternalStorageDirectory() + File.separator + ConstantUtil.FILE_DIR;
                    File dir = new File(path);
                    if (!dir.exists())
                    {
                        dir.mkdirs();
                    }

                    String fileName = url.substring(url.lastIndexOf("/")+1);
                    mFile = new File(dir, fileName);
                    if (mFile.exists()) {
                        LogUtil.all(String.format("%s已经保存过了", fileName));
                        return Observable.just(Uri.fromFile(mFile));
                    }
                    FileInputStream fis = new FileInputStream(file.getAbsolutePath());

                    int byteread = 0;
                    byte[] buf = new byte[1444];

                    FileOutputStream fos = new FileOutputStream(mFile.getAbsolutePath());
                    while ((byteread = fis.read(buf)) != -1)
                    {
                        fos.write(buf, 0, byteread);
                    }

                    fos.close();
                    fis.close();
                } catch (Exception e)
                {
                    e.printStackTrace();
                    LogUtil.all("图片下载失败");
                }

                //更新本地图库
                Uri uri = Uri.fromFile(mFile);
                Intent mIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                context.sendBroadcast(mIntent);


                return Observable.just(uri);
            }
        }).subscribeOn(Schedulers.io());
    }
}
