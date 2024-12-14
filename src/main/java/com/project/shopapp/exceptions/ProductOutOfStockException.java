package com.project.shopapp.exceptions;

public class ProductOutOfStockException extends Exception{
    public ProductOutOfStockException(String message){
        super(message);
    }
}
