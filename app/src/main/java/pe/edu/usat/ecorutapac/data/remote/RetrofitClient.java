package pe.edu.usat.ecorutapac.data.remote;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import pe.edu.usat.ecorutapac.data.remote.api.ApiService;
import pe.edu.usat.ecorutapac.util.SessionManager;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    // 127.0.0.1 / localhost con adb reverse tcp:5000 tcp:5000 para dispositivo físico
    public static String BASE_URL = "http://127.0.0.1:5000/api/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static String authToken = null;

    public static void setToken(String token) {
        authToken = token;
    }

    public static void setBaseUrl(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        BASE_URL = url;
        retrofit = null;
        apiService = null;
    }

    public static synchronized ApiService getService(Context context) {
        if (apiService == null || retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder builder = original.newBuilder()
                                    .header("Accept", "application/json");

                            // Buscar token en memoria o SharedPreferences
                            String token = authToken;
                            if (TextUtils.isEmpty(token) && context != null) {
                                SessionManager sm = new SessionManager(context);
                                token = sm.getToken();
                            }

                            if (!TextUtils.isEmpty(token)) {
                                builder.header("Authorization", "Bearer " + token);
                            }

                            return chain.proceed(builder.build());
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
