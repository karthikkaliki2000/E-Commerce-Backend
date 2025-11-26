package com.act.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatQueryRouter {

    @Autowired
    private SqlQueryExecutor sqlQueryExecutor;

    public String route(String message) {
        if (message.toLowerCase().contains("select") || message.toLowerCase().contains("from")) {
            return sqlQueryExecutor.runDynamicQuery(message);
        }
        return "Query not recognized as SQL. Please rephrase or ask a product/order-related question.";
    }
}
