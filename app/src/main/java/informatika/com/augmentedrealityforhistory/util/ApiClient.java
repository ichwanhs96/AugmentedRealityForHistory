package informatika.com.augmentedrealityforhistory.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by USER on 8/29/2016.
 */

public class ApiClient {
    public static final String BASE_URL = "http://94.177.249.173:3000/api/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit==null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.followRedirects(false);
            builder.readTimeout(60, TimeUnit.SECONDS);
            builder.connectTimeout(60, TimeUnit.SECONDS);
            OkHttpClient httpClient = builder.build();

            retrofit = new Retrofit.Builder()
                    .client(httpClient)
                    .baseUrl(BASE_URL)
                    .build();
        }
        return retrofit;
    }
}
