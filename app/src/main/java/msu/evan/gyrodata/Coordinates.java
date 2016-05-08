package msu.evan.gyrodata;

/**
 * Created by Evan on 2/26/2016.
 */
public class Coordinates {

    public double STEP_LENGTH = 0.75; //0.75 meters
    private double x;
    private double y;

    public Coordinates(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Coordinates(double x) {
        this.x = x;
        y = 0.0f;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(float x){
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Coordinates getCoord() {
        return new Coordinates(x,y);
    }


}
