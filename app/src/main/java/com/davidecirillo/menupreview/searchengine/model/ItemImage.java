package com.davidecirillo.menupreview.searchengine.model;


public class ItemImage {

    private String contextLink;
    private int byteSize;
    private String thumbnailLink;

    public ItemImage(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public String getContextLink() {
        return contextLink;
    }

    public int getByteSize() {
        return byteSize;
    }

    public String getThumbnailLink() {
        return thumbnailLink;
    }

    @Override
    public String toString() {
        return "ItemImage{" +
                "contextLink='" + contextLink + '\'' +
                ", byteSize=" + byteSize +
                ", thumbnailLink='" + thumbnailLink + '\'' +
                '}';
    }
}
