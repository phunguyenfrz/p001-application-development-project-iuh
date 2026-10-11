package com.petshop.dao;

public interface AuditRepository {
    void insert(String accountId, String action, String targetTable, String targetId, String note);
}
