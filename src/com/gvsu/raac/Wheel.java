package com.gvsu.raac;

import android.content.Context;

import static android.opengl.GLES10.*;

public class Wheel implements Drawable{

    private MeshObject wheel;

    public Wheel(Context context) {
        wheel = new MeshObject(context, "wheel.off");
    }
    @Override
    public void draw(Object... data) {
        glPushMatrix();
        wheel.draw();
        glPopMatrix();
    }
}
