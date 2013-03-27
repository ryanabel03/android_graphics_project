package com.gvsu.raac;

import android.content.Context;

import static android.opengl.GLES10.*;

public class Hand implements Drawable{
    private MeshObject hand;

    public Hand(Context context) {
        hand = new MeshObject(context, "hand.off");
    }

    @Override
    public void draw(Object... data) {
        glPushMatrix();
        hand.draw();
        glPopMatrix();
    }
}
