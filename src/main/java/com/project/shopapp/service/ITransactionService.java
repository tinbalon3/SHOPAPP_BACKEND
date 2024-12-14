package com.project.shopapp.service;

import com.project.shopapp.models.Transactions;
import jakarta.servlet.http.HttpServletRequest;

public interface ITransactionService {
    Transactions createTransaction(HttpServletRequest request);
    void saveTransaction(Transactions transactions);
    void deleteTransaction(Transactions transactions);
}
