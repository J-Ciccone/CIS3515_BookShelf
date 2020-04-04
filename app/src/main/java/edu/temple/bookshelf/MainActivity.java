package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface {
    private boolean twoPanes;
    private String searchUrl = "https://kamorris.com/lab/abp/booksearch.php?search=";

    RequestQueue requestQueue;

    final String BOOK_KEY = "BOOK_Key";
    final String BOOK_SELECTED ="BOOK_SELECTED";
    final String SELECTED = "SELECTED";

    FragmentTransaction fragmentTransaction;
    FragmentManager fragmentManager;

    boolean isSelected = false;

    Button searchButton;
    ArrayList<Book> books;
    BookDetailsFragment detailsFragment;
    BookListFragment listFragment;
    int selectedPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchButton = findViewById(R.id.searchButton);
        twoPanes = (findViewById(R.id.detailFrame) != null);
        if(savedInstanceState != null){
            books = (ArrayList<Book>)savedInstanceState.getSerializable(BOOK_KEY);
            selectedPosition = savedInstanceState.getInt(BOOK_SELECTED);
        }else{
            books = new ArrayList<>();
        }

        requestQueue = Volley.newRequestQueue(this);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        if(savedInstanceState != null){
            listFragment = BookListFragment.newInstance(books);
            if(savedInstanceState.getBoolean(SELECTED)){
                if(twoPanes){
                    detailsFragment = BookDetailsFragment.newInstance(books.get(savedInstanceState.getInt(BOOK_SELECTED)));
                    fragmentTransaction.add(R.id.detailFrame,detailsFragment)
                        .add(R.id.bookFrame,listFragment).commit();
                }else{
                    detailsFragment = BookDetailsFragment.newInstance(books.get(savedInstanceState.getInt(BOOK_SELECTED)));
                    fragmentTransaction.add(R.id.bookFrame,detailsFragment);
                    fragmentTransaction.commit();
                }
            }else{

            }
        }else{
            listFragment = BookListFragment.newInstance(books);
            fragmentTransaction.add(R.id.bookFrame,listFragment);
            fragmentTransaction.commit();
        }


        if (twoPanes){
            if(savedInstanceState != null){
                detailsFragment = BookDetailsFragment.newInstance(books.get(savedInstanceState.getInt(BOOK_SELECTED)));
            }else {
                detailsFragment = new BookDetailsFragment();
            }
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.detailFrame, detailsFragment);
            fragmentTransaction.commit();
        }

        searchButton.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            TextView bookRequest = findViewById(R.id.bookSearch);
            if(!twoPanes){
                isSelected = false;
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.addToBackStack(null).replace(R.id.bookFrame, listFragment);
                transaction.commit();
            }
            String query = searchUrl + bookRequest.getText().toString();
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(query,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            books.clear();
                            try{
                                ArrayList<Book> newBooks= new ArrayList<>();
                                // Loop through the array elements
                                for(int i=0;i<response.length();i++){
                                    // Get current json object
                                    JSONObject book = response.getJSONObject(i);

                                    // Get the current student (json object) data
                                    int id = book.getInt("book_id");
                                    String title = book.getString("title");
                                    String author = book.getString("author");
                                    String imgUrl = book.getString("cover_url");
                                    Book tempBook = new Book(id,title,author,imgUrl);
                                    books.add(tempBook);
                                }
                                listFragment.updateBooks(books);

                            }catch (JSONException e){
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                            System.out.println(error.toString());
                        }
                    });

            requestQueue.add(jsonArrayRequest);

        }


    });

}


    @Override
    public void bookSelected(int position) {
        selectedPosition = position;
        isSelected = true;
        Book book = books.get(position);
        if(twoPanes) {
            detailsFragment.update(book.getTitle(),book.getAuthor(), book.getCoverUrl());
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.addToBackStack(null).replace(R.id.bookFrame, BookDetailsFragment.newInstance(book));
            transaction.commit();
        }

    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(BOOK_KEY,books);
        outState.putInt(BOOK_SELECTED, selectedPosition);
        outState.putBoolean(SELECTED,isSelected);
        super.onSaveInstanceState(outState);
    }

}
