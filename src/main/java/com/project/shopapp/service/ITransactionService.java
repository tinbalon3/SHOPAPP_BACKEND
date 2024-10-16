package com.project.shopapp.service;

import com.project.shopapp.models.Transactions;
import jakarta.servlet.http.HttpServletRequest;

public interface ITransactionService {
    Transactions createTransaction(HttpServletRequest request);
}
