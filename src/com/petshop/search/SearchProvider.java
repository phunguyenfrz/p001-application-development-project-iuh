package com.petshop.search;

import com.petshop.model.Role;
import com.petshop.model.SearchResult;

import java.util.List;
import java.util.Set;

public interface SearchProvider {
    String getGroupName();

    Set<Role> visibleTo();

    List<SearchResult> search(String keyword);
}
