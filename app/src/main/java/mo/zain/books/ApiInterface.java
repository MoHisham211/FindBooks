package mo.zain.books;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;



/**
 * Interface for Querying the Url using Retrofit.
 */
public interface ApiInterface {

    @GET("v1/volumes")
    Call<ResponseBody> getlist(@Query("q") String str);

}
