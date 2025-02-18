package com.MarketingMVP.AllVantage.DTOs.UserEntity.Employee;

import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import org.springframework.stereotype.Service;

import java.util.function.Function;
@Service
public class EmployeeDTOMapper implements Function<Employee, EmployeeDTO> {
    @Override
    public EmployeeDTO apply(Employee employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getUsername(),
                employee.getEmail(),
                employee.getRole(),
                employee.getPhoneNumber(),
                employee.getCreationDate(),
                employee.isEnabled(),
                employee.isLocked()
        );
    }
}
