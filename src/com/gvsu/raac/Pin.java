package com.gvsu.raac;

import android.content.Context;

import static android.opengl.GLES10.*;

public class Pin implements Drawable {

    private MeshObject pin;

    public Pin(Context context) {
        pin = new MeshObject(context, "test.off");
    }
    @Override
    public void draw(Object... data) {
        glPushMatrix();
        pin.draw();
        glPopMatrix();

    }
}
