package com.evilbeast.meizi.network.Api;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Author: sumary
 */
public interface MeiZiApi  {
    String BASE_URL = "http://www.mzitu.com/";

    @GET("{type}/page/{pageNum}")
    Observable<ResponseBody> getMeiziData(@Path("type") String type, @Path("pageNum") int pageNum);

    @GET("{type}/comment-page-{page}#comments")
    Observable<ResponseBody> getHomeMeiziApi(@Path("type") String type, @Path("page") int page);

    @GET("{groupId}")
    Observable<ResponseBody> getGroupImages(@Path("groupId") int groupId);

    @GET("{groupId}/{pos}")
    Observable<ResponseBody> getGroupImageData(@Path("groupId") int groupId, @Path("pos") int pos);
}
