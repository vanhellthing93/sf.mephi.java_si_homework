package sf.mephi.study.otp.model;

public class User {

    private String login;
    private String encryptedPassword;
    private String salt;
    private Role role;

    public User(String login, String encryptedPassword, String salt, Role role) {
        this.login = login;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.role = role;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", role=" + role +
                '}';
    }

    public enum Role {
        ADMIN,
        USER
    }
}