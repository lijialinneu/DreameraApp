package neu.dreamerajni.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.filter.BaseFilter;
import neu.dreamerajni.filter.FilterFactory;

/**
 * Created by froger_mcs on 11.11.14.
 */
public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int itemsCount = 8;

    private ImageView photoView;
    private Bitmap photoBitmap;
    public static Bitmap dstBitmap = null;

    public PhotoFiltersAdapter(Context context,ImageView photoView, Bitmap photoBitmap) {
        this.context = context;
        this.photoView = photoView;
        this.photoBitmap = photoBitmap;
        this.dstBitmap = photoBitmap;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        lp.width = wm.getDefaultDisplay().getWidth()/4;
        view.setLayoutParams(lp);
        return new PhotoFilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        PhotoFilterViewHolder holder = (PhotoFilterViewHolder) viewHolder;
        final FilterFactory ff = new FilterFactory(photoBitmap);
        holder.filterTextView.setText(ff.getFilterType(position));
        holder.filterImageView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                BaseFilter bf = ff.createFilter(position);
                dstBitmap = bf.filterBitmap();
                photoView.setImageBitmap(dstBitmap);
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
        @Bind(R.id.id_filterText)
        TextView filterTextView;

        public PhotoFilterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
