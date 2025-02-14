package com.MarketingMVP.AllVantage.Entities.UserEntity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Client")
public class Client extends UserEntity{



}
