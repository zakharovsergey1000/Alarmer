package com.abc.greendaoexample.db;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table "REPO".
 */
public class Repo {

    private Long id;
    /** Not-null value. */
    private String title;
    private String language;
    private Integer watchers_count;
    private long userId;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Repo() {
    }

    public Repo(Long id) {
        this.id = id;
    }

    public Repo(Long id, String title, String language, Integer watchers_count, long userId) {
        this.id = id;
        this.title = title;
        this.language = language;
        this.watchers_count = watchers_count;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getTitle() {
        return title;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getWatchers_count() {
        return watchers_count;
    }

    public void setWatchers_count(Integer watchers_count) {
        this.watchers_count = watchers_count;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
