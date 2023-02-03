package com.tcc.pathfinderapi.objects;

public class Coordinate {

    private int x;
    private short y;
    private int z;


    /**
     * @param x
     * @param y
     * @param z
     */
    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = (short) y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    /**
     * Finds squared euclidean distance between current coordinate and other
     * @param other
     * @return
     */
    public double squaredDistance(Coordinate other) {
        int xDiff = this.getX() - other.getX();
        int yDiff = this.getY() - other.getY();
        int zDiff = this.getZ() - other.getZ();
        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }

    /**
     * Finds euclidean distance between current coordinate and other
     * @param other
     * @return
     */
    public double distance(Coordinate other) {
        return Math.sqrt(squaredDistance(other));
    }






    @Override
    public String toString() {
        return "Coordinate [x=" + x + ", y=" + y + ", z=" + z + "]";
    }

}
