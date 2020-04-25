package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface, BookDetailsFragment.PlayButtonInterface {

    private static final String BOOKS_KEY = "books";
    private static final String SELECTED_BOOK_KEY = "selectedBook";
    private static final Boolean PLAYING = true;
    private static final Boolean PAUSED = false;
    private static final Boolean NEW_FRAGMENT = false;
    private static final String PLAY_STATUS = "play_status";
    private static final String SEEK_PROGRESS = "seek_progress";
    private static final String DURATION = "duration";

    FragmentManager fm;

    int duration;

    boolean twoPane;
    boolean playWasClicked;
    boolean playStatus;
    Intent bindIntent;

    BookListFragment bookListFragment;
    BookDetailsFragment bookDetailsFragment;

    ArrayList<Book> books;
    RequestQueue requestQueue;
    Book selectedBook;

    EditText searchEditText;

    Handler mediaControlHandler;

    SeekBar mediaSeekBar;

    Uri bookUri;
    File bookFile;
    Button stopButton;

    boolean connected;
    AudiobookService.MediaControlBinder mediaControlBinder;

    ServiceConnection bookServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaControlBinder = (AudiobookService.MediaControlBinder) service;
            mediaControlBinder.setProgressHandler(mediaControlHandler);
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
        if (savedInstanceState != null) {
            books = savedInstanceState.getParcelableArrayList(BOOKS_KEY);
            selectedBook = savedInstanceState.getParcelable(SELECTED_BOOK_KEY);
            playStatus = savedInstanceState.getBoolean(PLAY_STATUS);
            duration = savedInstanceState.getInt(DURATION);
        }
        else{
            books = new ArrayList<Book>();
        }
        mediaControlHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                final AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
                mediaSeekBar.setMax(duration);
                if(mediaControlBinder.isPlaying()){
                    mediaSeekBar.setProgress(bookProgress.getProgress());
                    bookUri = bookProgress.getBookUri();
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

        bindIntent = new Intent(this, AudiobookService.class);
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
                        if(selectedBook != null){
                            updatePlayStatus(bookDetailsFragment,PAUSED);
                        }
                    }else{
                        mediaControlBinder.pause();
                        if(selectedBook != null){
                            if(playWasClicked){
                                bookDetailsFragment.titleTextView.setText(R.string.now_playing);
                                bookDetailsFragment.titleTextView.append(selectedBook.getTitle());
                            }
                        }
                    }
                }
            }
        });

        stopButton = findViewById(R.id.stopAudioButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected){
                    playWasClicked = false;
                    mediaControlBinder.stop();
                    if(selectedBook!=null){
                        bookDetailsFragment.titleTextView.setText(selectedBook.getTitle());
                        mediaSeekBar.setProgress(0);
                    }
                    stopService(bindIntent);
                }
            }
        });

        /*
        If we previously saved a book search and/or selected a book, then use that
        information to set up the necessary instance variables
         */

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
            if (selectedBook != null){
                if(playStatus){
                    bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook, PLAYING);
                }else{
                    bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook, NEW_FRAGMENT);
                }
            }
            else{
                bookDetailsFragment = new BookDetailsFragment();
            }
            fm.beginTransaction()
                    .replace(R.id.container2, bookDetailsFragment)
                    .commit();


        } else {
            if (selectedBook != null) {
                if(playStatus){
                        bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook, PLAYING);
                }else{
                    bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook, NEW_FRAGMENT);
                }
                fm.beginTransaction()
                        .replace(R.id.container1, bookDetailsFragment)
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
            bookDetailsFragment = BookDetailsFragment.newInstance(selectedBook, NEW_FRAGMENT);
            fm.beginTransaction()
                    .replace(R.id.container1, bookDetailsFragment)
                    // Transaction is reversible
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void playButtonClicked(int id) {
        if(connected){
            if(mediaControlBinder.isPlaying()){
                stopButton.callOnClick();
                startService(bindIntent);
                playWasClicked = true;
            }else{
                startService(bindIntent);
                playWasClicked = true;
                if(mediaSeekBar.getProgress() == 0){
                    mediaControlBinder.play(id);
                }else{
                    mediaControlBinder.pause();
                }
            }
            if(selectedBook !=null){
                updatePlayStatus(bookDetailsFragment, PLAYING );
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save previously searched books as well as selected book
        outState.putParcelableArrayList(BOOKS_KEY, books);
        outState.putParcelable(SELECTED_BOOK_KEY, selectedBook);
        outState.putInt(SEEK_PROGRESS,mediaSeekBar.getProgress());
        outState.putInt(DURATION, duration);
        if(connected){
            if(mediaControlBinder.isPlaying()){
                outState.putBoolean(PLAY_STATUS,true);
            }else{
                outState.putBoolean(PLAY_STATUS,false);

            }
        }

    }

    public void updatePlayStatus(BookDetailsFragment detailsFragment, boolean playing) {
        if(playing) {
            detailsFragment.titleTextView.setText(R.string.now_playing);
            detailsFragment.titleTextView.append(selectedBook.getTitle());
        }else {
            detailsFragment.titleTextView.setText(R.string.pause_playing);
            detailsFragment.titleTextView.append(selectedBook.getTitle());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(bookServiceConnection);
    }
}
