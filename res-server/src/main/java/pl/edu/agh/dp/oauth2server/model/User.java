package pl.edu.agh.dp.oauth2server.model;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Objects;

public class User {
    @BsonId
    @BsonProperty(value = "_id")
    private ObjectId id;
    @BsonProperty(value = "user_id")
    private String userID;
    private String username;
    private String mail;
    private List<String> posts;

    @BsonCreator
    public User(@BsonProperty(value = "_id") ObjectId id,
                @BsonProperty("user_id") String userID,
                @BsonProperty("username") String username,
                @BsonProperty("mail") String mail,
                @BsonProperty("posts") List<String> posts) {
        this.id = id;
        this.userID = userID;
        this.username = username;
        this.mail = mail;
        this.posts = posts;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id) &&
               Objects.equals(userID, user.userID) &&
               Objects.equals(username, user.username) &&
               Objects.equals(mail, user.mail) &&
               Objects.equals(posts, user.posts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userID, username, mail, posts);
    }

    @Override
    public String toString() {
        return "{ userID: " + userID + ", username: " + username + ", mail: " + mail + ", posts: " + posts.toString() + "}";
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<String> getPosts() {
        return posts;
    }

    public void setPosts(List<String> posts) {
        this.posts = posts;
    }
}
