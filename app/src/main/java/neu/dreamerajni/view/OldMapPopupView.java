package neu.dreamerajni.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import butterknife.ButterKnife;
import butterknife.OnClick;
import neu.dreamerajni.R;
import neu.dreamerajni.adapter.OldMapListAdapter;
import neu.dreamerajni.utils.BMapControlUtil;

/**
 * Created by 10405 on 2016/8/14.
 * This class extends android.view to show old map view in the MainActivity
 */

public class OldMapPopupView extends View {

    private Activity activity;
    private static LinearLayout linearLayout;
    private static RecyclerView gallery;
    private static ImageView flagView;
    private static SeekBar seekBar;
    private static float alpha = 100.f;
    private static BMapControlUtil bMapControlUtil;

    public OldMapPopupView(Context context, BMapControlUtil bMapControlUtil) {
        super(context);
        this.activity = (Activity) context;
        OldMapPopupView.bMapControlUtil = bMapControlUtil;
        ButterKnife.bind(this, activity);
        linearLayout = (LinearLayout) activity.findViewById(R.id.old_map_layout);
        gallery = (RecyclerView) activity.findViewById(R.id.id_oldMapList);
        seekBar = (SeekBar) activity.findViewById(R.id.id_map_alpha);
        showOldMap();
    }

    /**
     * Show old map recycler view
     */
    private void showOldMap(){
        linearLayout.setVisibility(VISIBLE);
        OldMapListAdapter oldMapListAdapter = new OldMapListAdapter(activity);
        gallery.setHasFixedSize(true);
        gallery.setAdapter(oldMapListAdapter);
        gallery.setLayoutManager(
                new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
    }


    /**
     * Old map clickListener
     */
    public static class MyImgClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            deleteOldSelected();       // 清除以前选中图片的对号小图标
            int i =  (int) v.getTag(); // 显示对号小图标
            flagView = (ImageView) gallery.findViewWithTag(i + "a");
            flagView.setVisibility(VISIBLE);
            bMapControlUtil.addOldMapOverlay(i);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    alpha = 100 - progress;
                    if(bMapControlUtil.oldMapOverlay != null) {
                        bMapControlUtil.oldMapOverlay.setTransparency(alpha / 100);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
            });

        }
    }

    /**
     * Delete old selected flag.
     */
    private static void deleteOldSelected(){
        if(flagView != null) {
            flagView.setVisibility(GONE);
        }
    }

    @OnClick(R.id.id_close_oldMap)
    void closeOldMap() {
        linearLayout.setVisibility(GONE);
    }
}
