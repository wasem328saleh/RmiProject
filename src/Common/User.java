package Common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {

    private String username;
    private String password;
    private String role; // "manager" or "employee"
    private String token;
    private Set<String> permissions; // مثل: "upload", "download"
    private String department;


    public User() {}

    // Constructor للمدير فقط
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.permissions = new HashSet<>();
    }

    // Constructor للموظف (يتضمن القسم والصلاحيات)
    public User(String username, String password, String role, String department, Set<String> permissions) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.department = department;
        this.permissions = permissions;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getDepartment() {
        return department;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", department='" + department + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}
