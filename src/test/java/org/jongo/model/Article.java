package org.jongo.model;

import java.util.Arrays;
import java.util.List;

public final class Article {

    private String title;
    @SuppressWarnings("unused")
    private String author;
    private List<String> tags;

    public Article(String title, String author, String... tags) {
        this.title = title;
        this.author = author;
        this.tags = Arrays.asList(tags);
    }

    private Article() {
        //used by jackson
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return tags;
    }
}
