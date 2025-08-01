package com.MarketingMVP.AllVantage.Entities.Responses.Error;


import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CustomErrorLog {

    private Date date;
    private String message;
    private UserEntity userEntity;
    private ErrorType errorType;
    private PlatformType platformType;
}
