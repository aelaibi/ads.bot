package ma.labs.bot;

import ma.labs.bot.core.RobotRunner;
import ma.labs.bot.utils.VersionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableRetry
@EnableAsync
@SpringBootApplication
public class Application implements AsyncConfigurer {
	private static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args)  {
		logger.info("\n\n ============================= {} "
				, VersionReader.getVersion());
		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
		RobotRunner robotRunner = ctx.getBean(RobotRunner.class);
		robotRunner.init();
		robotRunner.run();
	}
//	public static void main(String...strings) throws IOException{
//		ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, strings);
//		MediaHelperAPIConnector mediaHelperAPIConnector = ctx.getBean(MediaHelperAPIConnector.class);
//		int i =0;
//		for(String elem : list){
//			System.out.println("im at index : " + ++i);
//			final MediaHelperAPIResponse resp = mediaHelperAPIConnector.downloadByFileUrl(elem);
//			System.out.println(resp.getChecksum());
//			mediaHelperAPIConnector.uploadByFileUrl(elem);
//			TimeUtils.waitFor(2000);
//		}
//	}
	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(10);
		executor.setKeepAliveSeconds(10*60);
		executor.setThreadNamePrefix("labs_THREAD-");
		executor.initialize();
		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return null;
	}
}
