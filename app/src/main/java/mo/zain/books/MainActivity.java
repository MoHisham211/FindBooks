package mo.zain.books;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Button searchBtn;
    EditText inputText;
    RecyclerView recyclerView;
    private static ArrayList<BooksPogo> books = new ArrayList<>();
    private static final String BASE_URL = "https://www.googleapis.com/books/";
    private static final String TAB = MainActivity.class.getSimpleName();
    private static RecycleAdapter mRecycleAdapter;
    @SuppressLint("StaticFieldLeak")
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        if (CheckInterConnection(getApplication())){
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    String getSearchName=inputText.getText().toString().trim();
                    getSearchName.replace(" ","+");
                    if (!getSearchName.matches("")){
                        if (books != null) {
                            books.clear();
                        }
                        new BookListAsyncTask(getSearchName, getApplication()).execute();
                        hidekeyboard();
                    }else {
                        Toast.makeText(getApplication(), "No Name Found", Toast.LENGTH_LONG).show();
                        //progressBar.setVisibility(View.GONE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }else {
            Toast.makeText(this, "Chick Your Connection", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }

    }
    private void initialize(){
        searchBtn=findViewById(R.id.searchbtn);
        inputText=findViewById(R.id.inputSearch);
        //
        recyclerView=findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //
        progressBar=findViewById(R.id.progressBar);

    }

    private boolean CheckInterConnection(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private class BookListAsyncTask extends AsyncTask<ArrayList<BooksPogo>, Void, ArrayList<BooksPogo>> {

        // get text from EditText and store in text var.
        private String text;

        @SuppressLint("StaticFieldLeak")
        private Context context;

        BookListAsyncTask(String name, Context context) {
            this.text = name;
            this.context = context;
        }

        //onPreExecute method it is execute before doInBackground.
        @Override
        protected void onPreExecute() {
            // progressbar gets visible.
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(100);
            super.onPreExecute();
        }

        @SafeVarargs
        @Override
        protected final ArrayList<BooksPogo> doInBackground(ArrayList<BooksPogo>... voids) {
            // Retrofit object.
            Retrofit sent = new Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiInterface apiInterface = sent.create(ApiInterface.class);
            // get response body.
            Call<ResponseBody> getresponse = apiInterface.getlist(text);
            getresponse.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {

//                        progressBar.setVisibility(View.VISIBLE);

                        // get jsonObject.
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.getInt("totalItems") == 0) {
                            return;
                        }
                        // get jsonArray items from jsonObject.
                        JSONArray itemsArray = jsonObject.getJSONArray("items");
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject currentObject = itemsArray.getJSONObject(i);
                            // get json object from current object.
                            JSONObject volumeInfoObject = currentObject.getJSONObject("volumeInfo");
                            // get title from volumeInfoObject. And Store in title var.
                            String title = volumeInfoObject.getString("title");
                            String[] author;
                            // get author from volumeInfoObject. And store in author String Array.
                            JSONArray authorArray = volumeInfoObject.optJSONArray("authors");

                            if (authorArray != null) {
                                ArrayList<String> list = new ArrayList<>();
                                for (int a = 0; a < authorArray.length(); a++) {
                                    list.add(authorArray.get(a).toString());
                                }
                                author = list.toArray(new String[list.size()]);
                            } else {
                                // continue if author do not exits.
                                continue;
                            }
                            String description = "";
                            // get description if exits.
                            if (volumeInfoObject.optString("description") != null) {
                                description = volumeInfoObject.optString("description");
                            }
                            String infoLink = "";
                            // get infoLink if exits.
                            if (volumeInfoObject.optString("infoLink") != null) {
                                infoLink = volumeInfoObject.optString("infoLink");
                            }
                            // Add all object to books Arraylist.
                            books.add(new BooksPogo(title, author, description, infoLink));
                            // this is log message if need to check.
                            Log.i(TAB, title + Arrays.toString(author) + description + infoLink + i);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.i(TAB, t.toString());
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
            // return books ArrayList.
            return books;
        }


        // get books ArrayList And Update the UI.
        // This method runs on Main Thread.
        @Override
        protected void onPostExecute(ArrayList<BooksPogo> list) {
            progressBar.setVisibility(View.INVISIBLE);
            mRecycleAdapter = new RecycleAdapter(context, list);
            recyclerView.setAdapter(mRecycleAdapter);
            mRecycleAdapter.notifyDataSetChanged();
            // set onclick listener to recycle Adapter
            mRecycleAdapter.SetOnItemClickListener(books -> {
                // get current book Link.
                String link = books.getInfoLink();
                // start intent.
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            });
            super.onPostExecute(list);
        }
    }
    void hidekeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}