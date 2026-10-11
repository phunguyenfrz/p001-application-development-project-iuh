package com.petshop.controller;

import com.petshop.model.SearchResult;
import com.petshop.model.Role;
import com.petshop.search.GlobalSearchService;

import java.util.List;
import java.util.Objects;

public final class GlobalSearchControllerImpl implements GlobalSearchController {
    private final GlobalSearchService globalSearchService;

    public GlobalSearchControllerImpl(GlobalSearchService globalSearchService) {
        this.globalSearchService = Objects.requireNonNull(globalSearchService);
    }

    @Override
    public List<SearchResult> search(String keyword, Role role) {
        return globalSearchService.search(keyword, role);
    }
}
