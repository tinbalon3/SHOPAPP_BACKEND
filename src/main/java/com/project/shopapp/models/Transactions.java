package com.project.shopapp.models;

import com.project.shopapp.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "transactions")
@Getter
@Setter
public class Transactions implements Serializable {

    @Id
    private String id;

    @Column
    private String billNo;

    @Column
    private String transNo;

    @Column
    private String bankCode;

    @Column
    private String cardType;

    @Column
    private Integer amount;

    @Column
    private String currency;

    @Column
    private String bankAccountNo;

    @Column
    private String bankAccount;

    @Column
    private String refundBankCode;

    @Lob
    @Column
    private String reason;

    @Column
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
