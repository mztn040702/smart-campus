package com.campus.campus_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan("com.campus.campus_system.entity") // 明确指定实体类扫描路径
@EnableJpaRepositories("com.campus.campus_system.repository") // 明确指定Repository扫描路径
@SpringBootApplication
public class CampusSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusSystemApplication.class, args);
	}

}
