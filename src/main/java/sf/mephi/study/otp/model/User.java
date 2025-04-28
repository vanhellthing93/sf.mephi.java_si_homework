package sf.mephi.study.otp.model;

public class User {

    private String login;
    private String encryptedPassword;
    private Role role;

    public User(String login, String encryptedPassword, Role role) {
        this.login = login;
        this.encryptedPassword = encryptedPassword;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Переопределение методов equals и hashCode для корректного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!login.equals(user.login)) return false;
        if (!encryptedPassword.equals(user.encryptedPassword)) return false;
        return role == user.role;
    }

    @Override
    public int hashCode() {
        int result = login.hashCode();
        result = 31 * result + encryptedPassword.hashCode();
        result = 31 * result + role.hashCode();
        return result;
    }

    // Переопределение метода toString для удобного вывода информации о пользователе
    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", role=" + role +
                '}';
    }

    // Вложенный enum для ролей пользователя
    public enum Role {
        ADMIN,
        USER
    }
}