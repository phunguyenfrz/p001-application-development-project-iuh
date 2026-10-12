package com.petshop.dao;

import com.petshop.model.User;

public interface UserRepository {
    User findByCredentials(String username, String password);
}
