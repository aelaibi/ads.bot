package ma.labs.bot.data;


/**
 * Created by labs004 on 12/07/2016.
 */
public class RobotInfo extends BasicResp {
    private Robot data;

    public Robot getData() {
        return data;
    }

    public void setData(Robot data) {
        this.data = data;
    }


    @Override
    public String toString() {
        if(data != null)
            return data.toString();
        return super.toString();
    }

    public class JsonRobot extends Robot{
    }
}
