package com.MarketingMVP.AllVantage;

import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Repositories.Role.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AllVantageApplication {

	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(AllVantageApplication.class, args);

	}
/*
	@PostConstruct
	private void initProject()
	{
		initRoles();
	}
	private void initRoles()
	{
		roleRepository.save(new Role("EMPLOYEE"));
		roleRepository.save(new Role("ADMIN"));
		roleRepository.save(new Role("CLIENT"));
	}
*/

}
