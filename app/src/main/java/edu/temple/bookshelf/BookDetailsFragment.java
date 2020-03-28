package edu.temple.bookshelf;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

public class BookDetailsFragment extends Fragment{

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    private HashMap<String, String> book;
    private TextView title;
    private TextView author;

    final static String BOOK = "BOOK";

    public static BookDetailsFragment newInstance(HashMap<String,String> book) {
        BookDetailsFragment newDetailsFragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BOOK, book);
        newDetailsFragment.setArguments(bundle);
        return newDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null) {
            book = (HashMap<String,String>)bundle.get(BOOK);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_details, container, false);

        title = fragView.findViewById(R.id.title);
        author = fragView.findViewById(R.id.author);
        if(book == null){
            title.setText("No Book Selected");
            author.setText("No Book Selected");
        } else{
            title.setText(book.get(MainActivity.TITLE));
            author.setText(book.get(MainActivity.AUTHOR));
        }


        return fragView;
    }

    public void updateText(String title, String author){
        TextView titleView = getView().findViewById(R.id.title);
        TextView authorView = getView().findViewById(R.id.author);

        titleView.setText(title);
        authorView.setText(author);

    }

}
