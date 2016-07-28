package neu.dreamerajni.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.view.MarkerPopupWindowView.AsyncGetPicTask;
import neu.dreamerajni.view.MarkerPopupWindowView.MyImgClickListener;

/**
 * Created by 10405 on 2016/7/26.
 */

public class PhotoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<HashMap<String, Object>> picList;
    private int itemsCount;
    private String pictureUrl, pictureId;
    private AsyncGetPicTask asyncGetPicTask;
    private int date;

    public PhotoListAdapter(Context context, ArrayList<HashMap<String, Object>> picList) {
        this.context = context;
        this.picList = picList;
        itemsCount = calculateCount();
    }

    // 计算数量，排除那些没有图片的点
    public int calculateCount() {
        int count = 0;
        for(int i = 0; i < picList.size(); i++) {
            pictureUrl = picList.get(i).get("url").toString();
            if(pictureUrl != "null") {
                count ++;
            }
        }
        return count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_layout, parent, false);
        return new PhotoListAdapter.PhotoListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        PhotoListViewHolder holder = (PhotoListViewHolder) viewHolder;

        pictureUrl = picList.get(position).get("url").toString();
        if(pictureUrl != "null") {
            pictureId = picList.get(position).get("id").toString();

            holder.dateTextView.setText(picList.get(position).get("date").toString());
            holder.imgView.setImageResource(R.mipmap.ic_nothing);//先都设置为默认图片，
            holder.imgView.setTag(position);  //设置tag

            holder.selectImgView.setImageResource(R.mipmap.ic_selected); //设置选中的对号图片
            holder.selectImgView.setTag(position + "a");
            holder.imgView.setOnClickListener(new MyImgClickListener(pictureId, pictureUrl));

            asyncGetPicTask = new AsyncGetPicTask(holder.imgView, pictureUrl, pictureId);
            asyncGetPicTask.execute();
        }

//        if(pictureUrl != "null"){
//            asyncGetPicTask = new AsyncGetPicTask(holder.imgView, pictureUrl, pictureId);
//            asyncGetPicTask.execute();
//        }
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    class PhotoListViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.id_marker_img)
        ImageView imgView;
        @Bind(R.id.id_selected)
        ImageView selectImgView;
        @Bind(R.id.id_date)
        TextView dateTextView;

        public PhotoListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
