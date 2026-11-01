package com.shastage.store;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "server.port=0")
@Import(TestDatabaseConfiguration.class)
class StoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
