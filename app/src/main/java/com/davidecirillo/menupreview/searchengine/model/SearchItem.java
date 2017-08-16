package com.davidecirillo.menupreview.searchengine.model;


public class SearchItem {

    private ItemImage image;
    private String link;

    public SearchItem(ItemImage image) {
        this.image = image;
    }

    public ItemImage getImage() {
        return image;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "SearchItem{" +
                "image=" + image +
                ", link='" + link + '\'' +
                '}';
    }
}
