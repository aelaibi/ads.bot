package ma.labs.bot.utils;
/********************************************************
 *                                                      *
 *                    PIXITREND ROBOT                   *
 *                    BY labs GROUP                    *
 *                                                      *
 ********************************************************/

import ma.labs.bot.rules.FileManagerService;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by labs004 on 10/08/2016.
 */
@org.springframework.stereotype.Component
public class GUIHelper {

    private final static Logger logger = LoggerFactory.getLogger(FileManagerService.class);

    @Value("${mode:dev}")
    private String mode;
    @Value("${screenshot.active:true}")
    private boolean isScreenshotON;

    public  File captureScreenShot(WebDriver driver, WebElement element, String targetPath){
        if(!isScreenshotON)
            return null;
        try {
            WebElement root = driver.findElement(By.tagName("body"));
            String scrollHeight = root.getAttribute("scrollHeight");
            if(Utils.isProdMode(mode) && Integer.valueOf(scrollHeight)>12000){
                throw new IOException("screenshot are disabled for big page : height "+scrollHeight);
            }
            File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            if(element!=null) {
                BufferedImage fullImg = ImageIO.read(scrFile);
                Point point = element.getLocation();
                int y = point.getY();
                int elH=element.getSize().getHeight();
                int eleHeight =((elH + 100)>=fullImg.getHeight())?elH + 100:elH;
                BufferedImage eleScreenshot = fullImg.getSubimage(0, y, fullImg.getWidth(),
                        eleHeight);
                ImageIO.write(eleScreenshot, "png", scrFile);
            }
            Path target = Paths.get(targetPath);
            target.getParent().toFile().mkdirs();
            Files.copy(scrFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toFile();
        } catch (IOException e) {
            logger.error("can not create screenshot ",e);
        }
        return null;
    }

    public  void moveMouse(){
        moveMouse(false);
    }

    public  void moveMouse(boolean corner ) {
        if(Utils.isDevMode(mode))
            return;
        try {
            TimeUtils.waitFor(1000);
            Robot robot = new Robot();
            if (corner) {
                logger.debug("mouse is mouving to 2,100");
                robot.mouseMove(2,100);
            } else {
                for (int i = 0; i < Utils.getRandom(0, 5); i++) {
                    int x = Utils.getRandom(100, 1000);
                    int y = Utils.getRandom(200, 600)+100; // +100 browser header
                    robot.mouseMove(x, y);
                    TimeUtils.waitFor(500);
                    logger.debug("mouse is mouving to {}, {}", x, y);
                }
            }
            TimeUtils.waitFor(Utils.getRandom(2000, 5000));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
