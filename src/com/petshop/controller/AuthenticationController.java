package com.petshop.controller;

import com.petshop.model.User;

public interface AuthenticationController {
    User authenticate(String username, String password);
}
