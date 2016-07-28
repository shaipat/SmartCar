package com.example.maor.smartcar;

/**
 * Created by Maor on 14/06/2016.
 */

public class Line {

    private Point start;
    private Point end;

    Line(float x1,float y1,float x2,float y2)
    {
        start = new Point(x1,y1);
        end = new Point(x2,y2);
    }

    public Point getEnd() {
        return end;
    }

    public Point getStart() {
        return start;
    }
}
