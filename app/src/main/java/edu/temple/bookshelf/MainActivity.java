package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookSelectedInterface {
    public static final int NUMBOOKS = 10;
    public static final String TITLE = "TITLE";
    public static final String AUTHOR = "AUTHOR";
    ArrayList<HashMap<String,String>> books;
    String[] titles;
    String[] authors;
    HashMap<String,String>[] mapArray;
    boolean twoPanes;
    BookDetailsFragment detailsFragment;
    BookListFragment listFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titles = getResources().getStringArray(R.array.bookTitles);
        authors = getResources().getStringArray(R.array.bookAuthors);

        twoPanes = (findViewById(R.id.detailFrame) != null);

        books = generateBooks(titles,authors);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        listFragment = BookListFragment.newInstance(books);

        fragmentTransaction.add(R.id.bookFrame,listFragment);
        fragmentTransaction.commit();

        if (twoPanes){
            detailsFragment = new BookDetailsFragment();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.detailFrame, detailsFragment);
            fragmentTransaction.commit();
        }


    }
    public ArrayList<HashMap<String,String>> generateBooks(String[] titles, String[] authors){
        ArrayList<HashMap<String,String>> booksArray = new ArrayList<>();
        HashMap<String, String> book1 = new HashMap<>();
        HashMap<String, String> book2 = new HashMap<>();
        HashMap<String, String> book3 = new HashMap<>();
        HashMap<String, String> book4 = new HashMap<>();
        HashMap<String, String> book5 = new HashMap<>();
        HashMap<String, String> book6 = new HashMap<>();
        HashMap<String, String> book7 = new HashMap<>();
        HashMap<String, String> book8 = new HashMap<>();
        HashMap<String, String> book9 = new HashMap<>();
        HashMap<String, String> book10 = new HashMap<>();

        HashMap<String, String>[] mapArray = new HashMap[]{book1, book2, book3, book4, book5, book6, book7, book8, book9, book10};

        for(int i = 0; i < NUMBOOKS ; i++){
            mapArray[i].put(TITLE, titles[i]);
            mapArray[i].put(AUTHOR, authors[i]);
            booksArray.add(mapArray[i]);
        }
        return booksArray;
    }
    @Override
    public void bookSelected(int position) {
        HashMap<String,String> book = books.get(position);
        if(twoPanes) {
            detailsFragment.updateText(book.get(TITLE),book.get(AUTHOR));
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Fragment newFrag = BookDetailsFragment.newInstance(book);
            transaction.addToBackStack(null).replace(R.id.bookFrame, newFrag);
            transaction.commit();
        }

    }
}
