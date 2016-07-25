package neu.dreamerajni.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;

/**
 * Created by froger_mcs on 11.11.14.
 */
public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int itemsCount = 6;

    public PhotoFiltersAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        Bitmap  bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.img_filter_mock);
//        lp.width = bitmap.getWidth();
        WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        lp.width = wm.getDefaultDisplay().getWidth()/3;
        view.setLayoutParams(lp);
        return new PhotoFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        PhotoFilterViewHolder holder = (PhotoFilterViewHolder) viewHolder;
        holder.filterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("asdf " + position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    public static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.id_filterImage)
        ImageView filterImageView;

        public PhotoFilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
