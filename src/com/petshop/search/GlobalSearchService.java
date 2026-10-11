package com.petshop.search;

import com.petshop.model.Role;
import com.petshop.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GlobalSearchService {
    private final List<SearchProvider> providers;

    public GlobalSearchService(List<SearchProvider> providers) {
        this.providers = List.copyOf(Objects.requireNonNull(providers));
    }

    public List<SearchResult> search(String keyword, Role role) {
        if (keyword == null || keyword.isBlank() || role == null) {
            return List.of();
        }
        List<SearchResult> results = new ArrayList<>();
        for (SearchProvider provider : providers) {
            if (provider.visibleTo().contains(role)) {
                results.addAll(provider.search(keyword.trim()));
            }
        }
        return List.copyOf(results);
    }
}
