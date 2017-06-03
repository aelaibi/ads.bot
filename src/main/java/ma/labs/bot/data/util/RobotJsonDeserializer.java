package ma.labs.bot.data.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import ma.labs.bot.data.Robot;
import ma.labs.bot.data.RobotInfo;

import java.lang.reflect.Type;

/**
 * Created by labs004 on 13/07/2016.
 */
public class RobotJsonDeserializer implements JsonDeserializer<Robot> {


    public Robot deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            return null;
        }

        return jsonDeserializationContext.deserialize(jsonElement, RobotInfo.JsonRobot.class);
    }
}
