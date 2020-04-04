package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;

public class BookListFragment extends Fragment {
    private View fragView;
    private BookSelectedInterface bookSelectedInterface;
    private ArrayList<Book> books;
    ListView listView;
    BookAdapter bookAdapter;

    private final static String BOOKS = "BOOKS";

    interface BookSelectedInterface {
        void bookSelected(int position);
    }
    public BookListFragment() {
        // Required empty public constructor
    }
    public static BookListFragment newInstance(ArrayList<Book> books) {
        BookListFragment newBookListFragment = new BookListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BOOKS, books);
        newBookListFragment.setArguments(bundle);
        return newBookListFragment;
    }

    @Override
    public void onAttach(Context context) throws RuntimeException {
        super.onAttach(context);

        if(context instanceof BookSelectedInterface) {
            bookSelectedInterface = (BookSelectedInterface)context;
        } else {
            throw new RuntimeException(getContext() + "must implement BookSelectedInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if( bundle != null) {
            books = (ArrayList<Book>)bundle.get(BOOKS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.fragment_list, container, false);
        bookAdapter = new BookAdapter(getContext(),books);
        listView = fragView.findViewById(R.id.bookList);
        if(listView.getParent() != null) {
            ((ViewGroup)listView.getParent()).removeView(fragView);
        }
        listView.setAdapter(bookAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bookSelectedInterface.bookSelected(position);
            }
        });


        return fragView;
    }

    public void updateBooks(ArrayList<Book> newBooks){
        books = newBooks;
        listView.setAdapter(new BookAdapter(this.getContext(), books));
    }




}
