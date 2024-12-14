package com.project.shopapp.exceptions;

public class EmailNotRegisterException extends  Exception  {
    public EmailNotRegisterException(String email) {
        super(email);
    }
}
