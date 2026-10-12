package com.petshop.controller;

public interface AuditController {
    void log(String action, String targetTable, String targetId, String note);
}
