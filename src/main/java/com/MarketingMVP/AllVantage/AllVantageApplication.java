package com.MarketingMVP.AllVantage;

import com.MarketingMVP.AllVantage.Entities.Role.Role;
import com.MarketingMVP.AllVantage.Entities.Suit.Suit;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Client;
import com.MarketingMVP.AllVantage.Entities.UserEntity.Employee;
import com.MarketingMVP.AllVantage.Repositories.Role.RoleRepository;
import com.MarketingMVP.AllVantage.Repositories.UserEntity.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootApplication
@EnableCaching
public class AllVantageApplication {

	@Autowired
	private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(AllVantageApplication.class, args);
	}

	@PostConstruct
	private void initProject()
	{
		initRoles();
		initUsers();
	}
	private void initRoles() {
		if (!roleRepository.findAll().isEmpty()) return;

		roleRepository.save(new Role("EMPLOYEE"));
		roleRepository.save(new Role("ADMIN"));
		roleRepository.save(new Role("CLIENT"));
	}

	private void initUsers() {
		if (userRepository.findAll().isEmpty()) {
			Employee employee = new Employee();
			employee.setFirstName("Employee");
			employee.setLastName("Employee");
			employee.setUsername("employee");
			employee.setPhoneNumber("12345678");
			employee.setCreationDate(new Date());
			employee.setLocked(false);
			employee.setEnabled(true);
			employee.setEmail("amirchebbi60@gmail.com");
			employee.setPassword(passwordEncoder.encode("password123"));
			employee.setRole(roleRepository.getRoleByName("EMPLOYEE").orElse(new Role("EMPLOYEE")));
			employee.setSuits(new ArrayList<>());

			Client client = new Client();
			client.setFirstName("Client");
			client.setLastName("Client");
			client.setUsername("client");
			client.setPhoneNumber("12345678");
			client.setCreationDate(new Date());
			client.setLocked(false);
			client.setEnabled(true);
			client.setEmail("amirchebbi6@gmail.com");
			client.setPassword(passwordEncoder.encode("password123"));
			client.setRole(roleRepository.getRoleByName("CLIENT").orElse(new Role("CLIENT")));

			userRepository.save(employee);
			userRepository.save(client);
		};
	}

}
