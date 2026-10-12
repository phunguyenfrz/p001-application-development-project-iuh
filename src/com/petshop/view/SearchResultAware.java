package com.petshop.view;

public interface SearchResultAware {
    void showSearchResult(String id);

    default void showSearchKeyword(String keyword) {
    }
}
