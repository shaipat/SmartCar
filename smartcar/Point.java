package com.example.maor.smartcar;

/**
 * Created by Maor on 14/06/2016.
 */

public class Point {

    private float _x;
    private float _y;

    Point(float x,float y)
    {
        _x =x;
        _y = y;
    }

    public float x() {
        return _x;
    }


    public float y() {
        return _y;
    }
}
