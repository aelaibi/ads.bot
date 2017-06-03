package ma.labs.bot.utils.cmd;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by labs004 on 29/07/2016.
 */
public class CommandExecutor {
    private static Logger LOGGER = LoggerFactory.getLogger(CommandExecutor.class);

    public static void execute(String command, String... args){

        CommandLine cmdLine = new CommandLine(command);
        for(String arg : args){
            cmdLine.addArgument(arg);
        }
        DefaultExecutor executor = new DefaultExecutor();
        LOGGER.info("run command {}",cmdLine.toString());
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
            LOGGER.error("got warning executing"+cmdLine.toString(),e );
        }

    }
    public static List<String> executeAndGetOutPut(String command, String... args){

        CommandLine cmdLine = new CommandLine(command);
        for(String arg : args){
            cmdLine.addArgument(arg);
        }
        DefaultExecutor executor = new DefaultExecutor();
        CollectingLogOutputStream out= new CollectingLogOutputStream ();
        PumpStreamHandler psh = new PumpStreamHandler(out);
        executor.setStreamHandler(psh);
        LOGGER.info("run command {}",cmdLine.toString());
        try {
            executor.execute(cmdLine);
            return out.getLines();
        } catch (IOException e) {
            LOGGER.warn("warning!! executing {}", cmdLine.toString() );
            return out.getLines();
        }

    }
}
