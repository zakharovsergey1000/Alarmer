package biz.advancedcalendar.greendao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table USER_PROFILE.
 */
public class UserProfile {

    private Long id;
    /** Not-null value. */
    private String Email;
    private String AuthToken;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public UserProfile() {
    }

    public UserProfile(Long id) {
        this.id = id;
    }

    public UserProfile(Long id, String Email, String AuthToken) {
        this.id = id;
        this.Email = Email;
        this.AuthToken = AuthToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getEmail() {
        return Email;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getAuthToken() {
        return AuthToken;
    }

    public void setAuthToken(String AuthToken) {
        this.AuthToken = AuthToken;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
