package com.project.shopapp.exceptions;

import org.aspectj.weaver.ast.Or;

public class OrderException extends Exception{
    public OrderException(String message) {
        super(message);
    }
}
