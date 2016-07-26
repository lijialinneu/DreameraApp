//package neu.dreamerajni.adapter;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.Build;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.ImageView;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import neu.dreamerajni.R;
//import neu.dreamerajni.activity.HandleActivity;
//import neu.dreamerajni.filter.BlackFilter;
//
///**
// * Created by froger_mcs on 11.11.14.
// */
//public class PhotoFiltersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private Context context;
//    private int itemsCount = 6;
//
////    private SurfaceView photoView;
//    private Bitmap photoBitmap;
//    public Bitmap dstBitmap;
//
//    public PhotoFiltersAdapter(Context context, Bitmap photoBitmap) {
//        this.context = context;
//        this.photoBitmap = photoBitmap;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        final View view = LayoutInflater.from(context).inflate(R.layout.item_photo_filter, parent, false);
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        lp.width = wm.getDefaultDisplay().getWidth()/3;
//        view.setLayoutParams(lp);
//        return new PhotoFilterViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
//        PhotoFilterViewHolder holder = (PhotoFilterViewHolder) viewHolder;
//        holder.filterImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BlackFilter bf = new BlackFilter(photoBitmap);
//                dstBitmap = bf.filterBitmap();
//
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return itemsCount;
//    }
//
//    public static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {
//
//        @Bind(R.id.id_filterImage)
//        ImageView filterImageView;
//
//        public PhotoFilterViewHolder(View view) {
//            super(view);
//            ButterKnife.bind(this, view);
//        }
//    }
//}
