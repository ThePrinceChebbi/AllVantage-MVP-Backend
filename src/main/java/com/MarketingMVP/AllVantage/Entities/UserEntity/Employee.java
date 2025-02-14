package com.MarketingMVP.AllVantage.Entities.UserEntity;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("Employee")
public class Employee extends UserEntity{

}
