package com.project.shopapp.repositories;


import com.project.shopapp.models.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TransactionRepository  extends JpaRepository<Transactions, String> {
    Transactions findFirstByOrderByIdDesc();

    List<Transactions> findAllByUser_Id(Long userID);

    Transactions findByTransNo(String transNo);
}
