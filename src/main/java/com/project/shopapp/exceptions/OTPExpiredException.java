package com.project.shopapp.exceptions;

public class OTPExpiredException  extends  Exception {
    public OTPExpiredException(String message){
        super(message);
    }
}
