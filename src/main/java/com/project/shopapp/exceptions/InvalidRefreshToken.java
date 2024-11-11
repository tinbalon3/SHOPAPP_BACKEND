package com.project.shopapp.exceptions;

public class InvalidRefreshToken extends Exception{
    public InvalidRefreshToken(String mesaage){
        super(mesaage);
    }
}
