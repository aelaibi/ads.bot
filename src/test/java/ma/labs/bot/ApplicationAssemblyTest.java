package ma.labs.bot;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("file:src/main/assembly/application.properties")
public class ApplicationAssemblyTest {


	@Value("${mode}")
	private String mode;
	@Value("${robot.id}")
	private String idRobot ;

	@Test
	public void contextLoads() {
		Assert.assertEquals("prod", mode);
	}

	@Test
	public void contextLoadsWithRobotIdOne() {
		Assert.assertEquals("1", idRobot);
	}

}
