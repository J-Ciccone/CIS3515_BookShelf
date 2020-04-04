package edu.temple.bookshelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class BookAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Book> books;
    LayoutInflater mInflater;

    public BookAdapter(Context context, ArrayList<Book> books) {
        this.context = context;
        this.books = books;
        this.mInflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        Book book = this.books.get(position);
        View view;

        view = mInflater.inflate(R.layout.book_item, container, false);

        ((TextView) view.findViewById(R.id.title))
                .setText(book.getTitle());

        ((TextView) view.findViewById(R.id.author))
                .setText(book.getAuthor());

        return view;
    }
}
