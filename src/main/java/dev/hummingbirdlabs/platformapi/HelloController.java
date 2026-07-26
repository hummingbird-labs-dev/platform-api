package dev.hummingbirdlabs.platformapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

	@GetMapping("/hello")
	HelloResponse hello() {
		return new HelloResponse("Hello, world!");
	}

	record HelloResponse(String message) {
	}
}
