package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
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

import edu.temple.audiobookplayer.AudiobookService;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface, BookDetailsFragment.PlayButtonInterface {

    private static final String BOOKS_KEY = "books";
    private static final String SELECTED_BOOK_KEY = "selectedBook";
    private static final String NOW_PLAYING = "- Now Playing";


    FragmentManager fm;

    int duration;

    boolean twoPane;
    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;

    ArrayList<Book> books;
    RequestQueue requestQueue;
    Book selectedBook;

    EditText searchEditText;

    Handler mediaControlHandler;

    SeekBar mediaSeekBar;

    boolean connected;
    AudiobookService.MediaControlBinder mediaControlBinder;

    ServiceConnection bookServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaControlBinder = (AudiobookService.MediaControlBinder) service;
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };

    private final String SEARCH_API = "https://kamorris.com/lab/abp/booksearch.php?search=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaSeekBar = findViewById(R.id.seekBar);

        mediaControlHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                final AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
                mediaSeekBar.setMax(duration);
                if(mediaControlBinder.isPlaying()){
                    mediaSeekBar.setProgress(bookProgress.getProgress());
                }
                mediaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser){
                            mediaControlBinder.seekTo(progress);

                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                return false;
            }
        });


        searchEditText = findViewById(R.id.searchEditText);

        Intent bindIntent = new Intent(this, AudiobookService.class);
        bindService(bindIntent, bookServiceConnection, BIND_AUTO_CREATE);

        /*
        Perform a search
         */
        findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchBooks(searchEditText.getText().toString());
            }
        });

        findViewById(R.id.pauseAudioButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected){
                    if(mediaControlBinder.isPlaying()){
                        mediaControlBinder.pause();
                    }else{

                    }

                }
            }
        });



        findViewById(R.id.stopAudioButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected){
                    mediaControlBinder.stop();
                }
            }
        });

        /*
        If we previously saved a book search and/or selected a book, then use that
        information to set up the necessary instance variables
         */
        if (savedInstanceState != null) {
            books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
            selectedBook = savedInstanceState.getParcelable(SELECTED_BOOK_KEY);
        }
        else
            books = new ArrayList<Book>();

        twoPane = findViewById(R.id.container2) != null;
        fm = getSupportFragmentManager();

        requestQueue = Volley.newRequestQueue(this);

        /*
        Get an instance of BookListFragment with an empty list of books
        if we didn't previously do a search, or use the previous list of
        books if we had previously performed a search
         */
        bookListFragment = BookListFragment.newInstance(books);

        fm.beginTransaction()
                .replace(R.id.container1, bookListFragment)
                .commit();

        /*
        If we have two containers available, load a single instance
        of BookDetailsFragment to display all selected books.

        If a book was previously selected, show that book in the book details fragment
        *NOTE* we could have simplified this to a single line by having the
        fragment's newInstance() method ignore a null reference, but this way allow
        us to limit the amount of things we have to change in the Fragment's implementation.
         */
        if (twoPane) {
            if (selectedBook != null)
                bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook);
            else
                bookDetailsFragment = new BookDetailsFragment();

            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment)
                    .commit();
        } else {
            if (selectedBook != null) {
                fm.beginTransaction()
                        .replace(R.id.container1, BookDetailsFragment.newInstance(selectedBook))
                        // Transaction is reversible
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    /*
    Fetch a set of "books" from from the web service API
     */
    private void fetchBooks(String searchString) {
        /*
        A Volloy JSONArrayRequest will automatically convert a JSON Array response from
        a web server to an Android JSONArray object
         */
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(SEARCH_API + searchString, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                if (response.length() > 0) {
                    books.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject bookJSON;
                            bookJSON = response.getJSONObject(i);
                            books.add(new Book (bookJSON.getInt(Book.JSON_ID),
                                    bookJSON.getString(Book.JSON_TITLE),
                                    bookJSON.getString(Book.JSON_AUTHOR),
                                    bookJSON.getString(Book.JSON_COVER_URL),
                                    bookJSON.getInt(Book.JSON_DURATION)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    updateBooksDisplay();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.search_error_message), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonArrayRequest);
    };

    private void updateBooksDisplay() {
        /*
        Remove the BookDetailsFragment from the container after a search
        if it is the currently attached fragment
         */
        if (fm.findFragmentById(R.id.container1) instanceof BookDetailsFragment)
            fm.popBackStack();
        bookListFragment.updateBooksDisplay(books);
    }

    @Override
    public void bookSelected(int index) {
        selectedBook = books.get(index);
        duration = selectedBook.getDuration();
        if (twoPane)
            /*
            Display selected book using previously attached fragment
             */
            bookDetailsFragment.displayBook(selectedBook);
        else {
            /*
            Display book using new fragment
             */
            fm.beginTransaction()
                    .replace(R.id.container1, BookDetailsFragment.newInstance(selectedBook))
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void playButtonClicked(int id) {
        if(connected){
            mediaControlBinder.play(id);
            mediaControlBinder.setProgressHandler(mediaControlHandler);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save previously searched books as well as selected book
        outState.putParcelableArrayList(BOOKS_KEY, books);
        outState.putParcelable(SELECTED_BOOK_KEY, selectedBook);
    }
}
