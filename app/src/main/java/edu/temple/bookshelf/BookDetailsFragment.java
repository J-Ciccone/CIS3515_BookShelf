package edu.temple.bookshelf;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class BookDetailsFragment extends Fragment{

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    private Book book;
    private TextView title;
    private TextView author;
    private ImageView coverImage;

    final static String BOOK_TITLE = "BOOK_TITLE";
    final static String BOOK_AUTHOR = "BOOK_AUTHOR";
    final static String BOOK_IMG_URL = "BOOK_IMG_URL";
    final static String BOOK_ID = "BOOK_ID";



    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment newDetailsFragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BOOK_ID,book.getId());
        bundle.putString(BOOK_TITLE,book.getTitle());
        bundle.putString(BOOK_AUTHOR,book.getAuthor());
        bundle.putString(BOOK_IMG_URL,book.getCoverUrl());
        newDetailsFragment.setArguments(bundle);
        return newDetailsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null) {
            book = new Book(
                    bundle.getInt(BOOK_ID),
                    bundle.getString(BOOK_TITLE),
                    bundle.getString(BOOK_AUTHOR),
                    bundle.getString(BOOK_IMG_URL)
                    );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_details, container, false);

        title = fragView.findViewById(R.id.bookTitle);
        author = fragView.findViewById(R.id.bookAuthor);
        coverImage = fragView.findViewById(R.id.coverImage);

        if(book == null){
            title.setText("No Book Selected");
            author.setText("No Book Selected");
        } else{
            title.setText(book.getTitle());
            author.setText(book.getAuthor());
            Picasso.get().load(book.getCoverUrl()).into(coverImage);
        }


        return fragView;
    }

    public void update(String title, String author, String coverUrl){
        TextView titleView = getView().findViewById(R.id.bookTitle);
        TextView authorView = getView().findViewById(R.id.bookAuthor);
        ImageView coverImage = getView().findViewById(R.id.coverImage);
        titleView.setText(title);
        authorView.setText(author);
        Picasso.get().load(coverUrl).into(coverImage);

    }

}
