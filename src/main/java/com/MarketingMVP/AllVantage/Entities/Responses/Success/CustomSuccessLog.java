package com.MarketingMVP.AllVantage.Entities.Responses.Success;


import com.MarketingMVP.AllVantage.Entities.Responses.Error.ErrorType;
import com.MarketingMVP.AllVantage.Entities.UserEntity.UserEntity;
import com.MarketingMVP.AllVantage.Repositories.Account.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class CustomSuccessLog {

    private Date date;
    private Object data;
    private UserEntity userEntity;
    private PlatformType platformType;
}
