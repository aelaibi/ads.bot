package ma.labs.bot;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {


	@Value("${mode}")
	private String mode;


	@Test
	public void contextLoads() {
		Assert.assertEquals("dev", mode);
	}

}
