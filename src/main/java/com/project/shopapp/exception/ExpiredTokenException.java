package com.project.shopapp.exception;

public class ExpiredTokenException extends Exception{
    public ExpiredTokenException(String mesaage){
        super(mesaage);
    }
}
