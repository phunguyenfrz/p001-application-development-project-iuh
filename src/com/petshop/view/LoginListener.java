package com.petshop.view;

import com.petshop.model.User;

@FunctionalInterface
public interface LoginListener {
    void onLoginSuccess(User user);
}
