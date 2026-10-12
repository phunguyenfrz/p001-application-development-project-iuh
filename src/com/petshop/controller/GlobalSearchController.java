package com.petshop.controller;

import com.petshop.model.SearchResult;
import com.petshop.model.Role;

import java.util.List;

public interface GlobalSearchController {
    List<SearchResult> search(String keyword, Role role);
}
