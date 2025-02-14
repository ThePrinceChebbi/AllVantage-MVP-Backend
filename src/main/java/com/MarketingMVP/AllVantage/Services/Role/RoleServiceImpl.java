package com.MarketingMVP.AllVantage.Services.Role;


import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Exceptions.ResourceNotFoundException;
import com.MarketingMVP.AllVantage.Repositories.Role.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role fetchRoleByName(String roleName) {
        return roleRepository.getRoleByName(roleName).orElseThrow(
                ()-> new ResourceNotFoundException("The role with name : %s could not be found.")
        );
    }
}
