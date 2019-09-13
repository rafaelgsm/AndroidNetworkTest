package rafaelgsm.example.androidnetworktest;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


/**
 * Example of retrofit+gson+okhttp
 * <p>
 * -retrofit used to build requests and treat responses properly (type-aware code for your API)
 * -okhttp for configuring a client properly with interceptors n all
 * -gson to parse response data easily
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();


        ////////////////////////////////////////////////////////////////////////////////////////////
        // Define Logging Interceptor just for DEBUG mode
        // (You can see all requests in LogCat by filtering for "okhttp")
        ////////////////////////////////////////////////////////////////////////////////////////////

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }


        //....

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Some random interceptor for the header
        // (Here we are just adding some "Platform" in the header, with the value "mobile")
        ////////////////////////////////////////////////////////////////////////////////////////////
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request.Builder requestBuilder = chain.request().newBuilder()
                        .header("Platform", "mobile");

                return chain.proceed(requestBuilder.build());
            }

        };

        httpClientBuilder.addInterceptor(headerInterceptor);


        //....

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Building the okhttp client:
        ////////////////////////////////////////////////////////////////////////////////////////////
        int CONNECTION_TIMEOUT = 45;

        OkHttpClient okHttpClient = httpClientBuilder
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build();


        //....

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Building a Retrofit object to make everything work together.
        ////////////////////////////////////////////////////////////////////////////////////////////

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create(new Gson()))   // <-- This guy will parse the Json into Java objects for you
                .client(okHttpClient)   // <-- Attaching the okhttp client
                .build();

        YourEndpoints yourEndpoints = retrofit.create(YourEndpoints.class); // <--- This is the guy that will do the requests


        //.....


        //Adding a click listener to the TextView (Using lambda notation-- similar to es6 arrow functions)
        findViewById(R.id.tv_text).setOnClickListener(v -> {

                    //Get one post:
                    yourEndpoints.getPost(1).enqueue(new Callback<Post>() {
                        @Override
                        public void onResponse(Call<Post> call, retrofit2.Response<Post> response) {

                            //The request succeeded, but we still need to check for http response codes like 4xx 5xx etc

                            Post post = response.body();
                            Log.d("TESTE", "" + post);

                        }

                        @Override
                        public void onFailure(Call<Post> call, Throwable t) {

                            //The request failed before even reaching the server (internet connection or whatever)
                            Log.d("TESTE", "" + t);
                        }
                    });


                    //Get a list of posts:
//                    yourEndpoints.getPosts().enqueue(new Callback<List<Post>>() {
//                        @Override
//                        public void onResponse(Call<List<Post>> call, retrofit2.Response<List<Post>> response) {
//                            List<Post> posts = response.body();
//
//
//                            for (int i = 0; i < posts.size(); i++) {
//                                Log.d("TESTE", "" + posts.get(i));
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<List<Post>> call, Throwable t) {
//
//                        }
//                    });


                }   //Closing lambda

        );

    }


    //...............
    //...............


    interface YourEndpoints {
        @GET("posts")
        Call<List<Post>> getPosts();

        @GET("posts/{post_id}")
        Call<Post> getPost(@Path("post_id") Integer id);
    }

    /**
     * Model class for the post item.
     * SerializedName annotation allows you to map the attibutes for parging with Gson library.
     */
    class Post {
        @SerializedName("userId")
        Integer userId;

        @SerializedName("id")
        Integer id;

        @SerializedName("title")
        String title;

        @SerializedName("body")
        String body;

    }
}
