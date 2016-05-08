package msu.evan.gyrodata;

/**
 * Created by Evan on 12/17/2015.
 */
public class Characteristics {

    //Used for classifying sensors for sensor list in MainActivity of the application
    public int version;
    public String name;
    public int numberType;
    public float power_usage;
    public int max_fifo_size;

    public Characteristics (int version, String name, int numberType, float power_usage, int max_fifo_size)
    {
        this.version = version;
        this.name = name;
        this.numberType = numberType;
        this.power_usage = power_usage;
        this.max_fifo_size = max_fifo_size;
    }
}
