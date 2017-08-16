package com.davidecirillo.menupreview.scan;

import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.davidecirillo.menupreview.R;
import com.davidecirillo.menupreview.searchengine.model.HeaderItem;
import com.davidecirillo.menupreview.searchengine.model.ItemImage;
import com.davidecirillo.menupreview.searchengine.model.SearchResultContainer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE_IMAGE = 1;
    private static final int ITEM_VIEW_TYPE_HEADER = 0;

    private List<Object> mItems;
    private Callbacks mCallbacks;
    private SearchResultContainer mSearchResultContainer;
    private int mImageWith;
    private int mImageHeight;

    public SearchResultAdapter(Callbacks callbacks) {
        mCallbacks = callbacks;
        mItems = new ArrayList<>();
    }

    public void showResults(SearchResultContainer searchResultContainer, @Nullable HeaderItem headerItem) {
        if (searchResultContainer.getImages() != null) {

            mSearchResultContainer = searchResultContainer;

            mItems.clear();

            if (headerItem != null) {
                mItems.add(headerItem);
            }
            mItems.addAll(searchResultContainer.getImages());

            notifyDataSetChanged();
        }
    }

    public void setImageSize(int width, int height){
        mImageWith = width;
        mImageHeight = height;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM_VIEW_TYPE_HEADER:
                viewHolder = new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_header, parent, false));
                break;
            case ITEM_VIEW_TYPE_IMAGE:
                viewHolder = new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_image, parent, false));
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            ItemImage itemImage = (ItemImage) mItems.get(position);
            ((ImageViewHolder) holder).bind(itemImage);
        } else if (holder instanceof HeaderViewHolder) {
            HeaderItem headerItem = (HeaderItem) mItems.get(position);
            ((HeaderViewHolder) holder).bind(headerItem);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(final int itemPos) {
        int itemType = -1;

        Object o = mItems.get(itemPos);

        if (o instanceof ItemImage) {
            itemType = ITEM_VIEW_TYPE_IMAGE;
        } else if (o instanceof HeaderItem) {
            itemType = ITEM_VIEW_TYPE_HEADER;
        }
        return itemType;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int span = 1;
                    if (mItems.get(position) instanceof HeaderItem) {
                        span = 3;
                    }
                    return span;
                }
            });
        }
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image);
        }

        void bind(final ItemImage itemImage) {
            Picasso.with(itemView.getContext())
                    .load(itemImage.getThumbnailLink())
                    .into(mImageView);

            if(mImageWith != 0 && mImageHeight != 0){
                mImageView.getLayoutParams().width = mImageWith;
                mImageView.getLayoutParams().height = mImageHeight;
            }

            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    HeaderItem headerItem = (HeaderItem) mItems.get(0);

                    ArrayList<String> strings = mSearchResultContainer.toImageLinkList();

                    mCallbacks.onImageClick(headerItem.getQuery(), strings, strings.indexOf(itemImage.getThumbnailLink()));
                }
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text);
        }

        void bind(HeaderItem headerItem) {
            mTextView.setText(headerItem.getQuery());
        }
    }

    public interface Callbacks {
        void onImageClick(String text, ArrayList<String> imageList, int selectedPosition);
    }
}
