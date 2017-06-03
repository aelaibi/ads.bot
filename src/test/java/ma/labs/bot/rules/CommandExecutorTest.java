package ma.labs.bot.rules;

import ma.labs.bot.utils.cmd.CommandExecutor;
import org.junit.Assert;
import org.junit.Test;

/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

/**
 * Created by labs004 on 29/07/2016.
 */
public class CommandExecutorTest {
    @Test
    public void executeJavaVersionShouldBe18() throws Exception {
      Assert.assertTrue(CommandExecutor.executeAndGetOutPut("java" , "-version").get(0).startsWith("java version \"1.8."));
    }

    @Test
    public void executeLS() throws Exception {
        CommandExecutor.execute("ping", "TEST_PIXI.5555");
    }

}