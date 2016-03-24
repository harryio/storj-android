package com.harryio.storj.model;

public class UserStatus {
    public String email;
    public String created;
    public boolean activated;

    public String getEmail() {
        return email;
    }

    public String getCreated() {
        return created;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public String toString() {
        return "SignUpResult{" +
                "email='" + email + '\'' +
                ", created='" + created + '\'' +
                ", activated=" + activated +
                '}';
    }
}
