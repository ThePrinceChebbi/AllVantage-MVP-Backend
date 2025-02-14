package com.MarketingMVP.AllVantage.Entities.UserEntity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Admin")
public class Admin extends UserEntity{



}
