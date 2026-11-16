package ru.shortLinkService.exceptions;

public class CompressedException extends Exception{
    public CompressedException(String message){
        super(message);
    }

    @Override
    public String getMessage(){
        return super.getMessage();
    }
}
