package com.gvsu.raac;

/**
 * Created with IntelliJ IDEA.
 * User: Alex
 * Date: 3/28/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatrixHelper {
    public static float[] getRotationMatrix(double pAngle, float x, float y, float z) {
        float angle = (float)Math.toRadians(pAngle);
        if(x > 0)
            return new float[]{ 1,0,0,0,
                    0,(float)Math.cos(angle),(float)Math.sin(angle),0,
                    0,(float)-Math.sin(angle),(float)Math.cos(angle),0,
                    0,0,0,1 };
        else if(y > 0)
            return new float[]{ (float)Math.cos(angle),0,(float)-Math.sin(angle),0,
                    0,1,0,0,
                    (float)Math.sin(angle),0,(float)Math.cos(angle),0,
                    0,0,0,1 };
        else
            return new float[]{ (float)Math.cos(angle),(float)Math.sin(angle),0,0,
                    (float)-Math.sin(angle),(float)Math.cos(angle),0,0,
                    0,0,1,0,
                    0,0,0,1 };
    }

    public static float[] getTranslationMatrix(float x, float y, float z) {
        return new float[]{ 1,0,0,0,
                0,1,0,0,
                0,0,1,0,
                x,y,z,1
        };
    }
}