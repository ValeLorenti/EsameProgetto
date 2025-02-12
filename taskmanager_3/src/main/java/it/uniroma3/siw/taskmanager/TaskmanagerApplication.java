package it.uniroma3.siw.taskmanager;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskmanagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(TaskmanagerApplication.class, args);
	}

	/** A reference to the environment of properties defined through the application.properties file.
	 * It is automatically wired before launch. */
}
