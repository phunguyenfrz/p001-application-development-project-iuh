package com.petshop.model;

public class User {
    public enum Role {
        OWNER("Chủ cửa hàng"),
        STAFF("Nhân viên");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String username;
    private final String fullName;
    private final Role role;

    public User(String username, String fullName, Role role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
