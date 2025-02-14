package com.MarketingMVP.AllVantage.Services.Role;


import com.MarketingMVP.AllVantage.Entities.Role.Role;

public interface RoleService {

    Role fetchRoleByName(final String roleName);
}
