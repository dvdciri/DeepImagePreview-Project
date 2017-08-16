package com.davidecirillo.menupreview.preview;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.davidecirillo.menupreview.R;

import java.util.ArrayList;
import java.util.List;

public class DetectionViewAdapter extends RecyclerView.Adapter<DetectionViewHolder> {

    private List<Detection> mItemList;
    private Listener mListener;

    public DetectionViewAdapter(Listener listener) {
        this.mListener = listener;
        this.mItemList = new ArrayList<>();
    }

    public void addDetection(Detection detection) {
        // If not in the list the add it at the top and notify
        if (!mItemList.contains(detection)) {
            mItemList.add(detection);
            notifyItemInserted(mItemList.size() - 1);
        } else {
            // If is contained then get the id of the position and put the new one at the same place
            int i = mItemList.indexOf(detection);
            mItemList.set(i, detection);
            notifyDataSetChanged();
        }
    }

    @Override
    public DetectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DetectionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(DetectionViewHolder holder, int position) {
        holder.bind(mItemList.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public interface Listener {
        void onItemClicked(String query);
    }
}
