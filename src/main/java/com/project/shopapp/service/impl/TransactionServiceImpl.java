package com.project.shopapp.service.impl;

import com.project.shopapp.models.Transactions;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.ITransactionService;
import com.project.shopapp.service.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements ITransactionService {
    private final IVNPayService vnPayService;

    private final UserRepository userRepository;
    @Override
    @Transactional
    public Transactions createTransaction(HttpServletRequest request) {

        Transactions transaction = new Transactions();
        transaction.setId(request.getParameter("vnp_TransactionNo"));
        transaction.setBillNo(request.getParameter("vnp_TxnRef"));
        transaction.setTransNo(request.getParameter("vnp_TransactionNo"));
        transaction.setBankCode(request.getParameter("vnp_BankCode"));
        transaction.setCardType(request.getParameter("vnp_CardType"));
        transaction.setAmount(Integer.parseInt(request.getParameter("vnp_Amount")));
        transaction.setCurrency("VND");
        transaction.setCreateDate(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        String orderInfo = request.getParameter("vnp_OrderInfo");
        String[] arrayInfo = orderInfo.split("-");
        Long userID = Long.parseLong(arrayInfo[0]);
        String reason = arrayInfo[1];
        transaction.setUser(userRepository.getById(userID));
        transaction.setReason(reason);
        return transaction;
    }
}
