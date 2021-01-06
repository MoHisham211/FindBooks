package mo.zain.books;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


/**
 * RecycleAdapter class.
 */
public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.RecycleViewHolder> {
    private static final String TAB = RecycleAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<BooksPogo> mlist;
    private LayoutInflater mInflater;
    private OnItemListener mOnItemClickListener;

    RecycleAdapter(Context context,ArrayList<BooksPogo> list){
        this.context = context;
        this.mlist = list;
        this.mInflater = LayoutInflater.from(context);
    }

    void SetOnItemClickListener(OnItemListener OnClickListener){
        this.mOnItemClickListener = OnClickListener;
    }

    @Override
    public RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list,parent,false);
        return new RecycleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecycleViewHolder holder, int position) {
        Log.i(TAB,"onBindViewHolder call");
        BooksPogo currentitem = mlist.get(position);
        holder.mTitleTextView.setText(currentitem.getTitle());
        holder.mAuthoeTextView.setText(currentitem.getAuthorNames());
        holder.mDescriptionTextView.setText(currentitem.getDescription());
        holder.bind(mlist.get(position),mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    static class RecycleViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;
        private TextView mAuthoeTextView;
        private TextView mDescriptionTextView;

        RecycleViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.book_title);
            mAuthoeTextView = itemView.findViewById(R.id.book_authors);
            mDescriptionTextView = itemView.findViewById(R.id.book_description);

        }

        void bind(BooksPogo books, OnItemListener OnItemListener){
            itemView.setOnClickListener(view->{
                OnItemListener.OnItemClick(books);
            });
        }
    }
}
