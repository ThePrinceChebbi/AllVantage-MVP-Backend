package com.MarketingMVP.AllVantage.DTOs.Authentication.Client;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClientRegisterDTO {

    //TODO: Fix all the fields to match the UserEntity fields

    private String name;

    private String reference;

    private String email;

    private String password;

    private String phoneNumber;
}
