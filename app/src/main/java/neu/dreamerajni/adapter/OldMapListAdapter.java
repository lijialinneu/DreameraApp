package neu.dreamerajni.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import neu.dreamerajni.R;
import neu.dreamerajni.view.OldMapPopupView;

/**
 * Created by 10405 on 2016/8/14.
 */

public class OldMapListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;


    public OldMapListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_layout, parent, false);
        return new OldMapListAdapter.OldMapListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        OldMapListViewHolder holder = (OldMapListAdapter.OldMapListViewHolder) viewHolder;

        String year;
        int resId;
        switch (position){
            case 0:
                year = "1930";
                resId = R.mipmap.map1;
                break;
            case 1:
                year = "1940";
                resId = R.mipmap.map2;
                break;
            case 2:
                year = "1950";
                resId = R.mipmap.map3;
                break;
            case 3:
                year = "1980";
                resId = R.mipmap.map4;
                break;
            case 4:
                year = "2000";
                resId = R.mipmap.map5;
                break;
            default:
                year = "";
                resId = R.mipmap.ic_nothing;
        }

        holder.dateTextView.setText(year);
        holder.imgView.setImageResource(R.mipmap.ic_nothing);//先都设置为默认图片，
        holder.imgView.setImageResource(resId);
        holder.imgView.setTag(position);  //设置tag
        holder.selectImgView.setImageResource(R.mipmap.ic_selected); //设置选中的对号图片
        holder.selectImgView.setTag(position + "a");
        holder.imgView.setOnClickListener(new OldMapPopupView.MyImgClickListener());
    }


    @Override
    public int getItemCount() {
        return 5;
    }

    class OldMapListViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.id_marker_img)
        ImageView imgView;
        @Bind(R.id.id_selected)
        ImageView selectImgView;
        @Bind(R.id.id_date)
        TextView dateTextView;

        public OldMapListViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
