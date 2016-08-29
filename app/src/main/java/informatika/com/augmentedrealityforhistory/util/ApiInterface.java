package informatika.com.augmentedrealityforhistory.util;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by USER on 8/29/2016.
 */

public interface ApiInterface {
    @Multipart
    @POST("Containers/{container}/upload")
    Call<ResponseBody> upload(@Header("Authorization") String auth_key, @Part MultipartBody.Part file, @Path("container") String container);
}
