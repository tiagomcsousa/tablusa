package io.github.tiagomcsousa.tablusa;

import org.springframework.boot.SpringApplication;

public class TestTablusaApplication {

	public static void main(String[] args) {
		SpringApplication.from(TablusaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
