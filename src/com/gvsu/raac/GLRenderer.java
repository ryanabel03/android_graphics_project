package com.gvsu.raac;


import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class GLRenderer implements Renderer {
    private final String TAG = getClass().getName();
    private final float SPH_SIZE = 0.5f; /* radius */

    private Box box;
    Drawable sphere;
    private Texture logo, wood;
    private Context mCtx;
    private boolean isLandscape, isPinching, lighting;
    private float[] sphRotation;
    private float[] handCF;
    private float ratio;
    private TransformationParams param;
    private float prevX, prevY, prevDist;
    private int scrWidth, scrHeight;
    
    private final static float lightAmb[] = {0.4f, 0.4f, 0.4f, 1f};
    private final static float lightDif[] = {1f, 1f, 1f, 1f};
    private final static float lightPos[] = {2.6f, 0.0f, 5.0f, 1f};
    private final static float lightDir[] = {0, 0, -1f, 0f};
    private final static float materialAmb[] = {0.3f, 0.3f, 0.3f, 1f};
    private final static float materialSpe[] = {0.7f, 1.0f, 0.7f, 1f};
    private boolean anim;
    private Pin pin;
    private Texture rubber;
    private Texture alley;
    private Hand hand;
    private Texture skin;
    private float handPosY = 0f;
    private float handAngle = 0;
    private TransformationParams handPars;
    private static final float MAX_HAND_POS = .5f;
    private boolean movingHandForward;
    private static final float MIN_HAND_POS = -.1f;

    private float[] randomX, randomY;
    private float[] randomXrot, randomYrot, randomZrot;
    private int numPins = 4;
    private Texture metal;
    private Texture ball;

    public GLRenderer(Context parent, TransformationParams p, TransformationParams handPars)
    {
        movingHandForward = true;
        mCtx = parent;
        anim = true;
        box = new Box(20, 20);
        sphere = new MeshObject(mCtx, "sphere.off");
        pin = new Pin(mCtx);
        hand = new Hand(mCtx);
        sphRotation = new float[16];
        handCF = new float[16];
        /* initialize with identity matrix */
        for (int k = 0; k < 16; k += 5) {
        	sphRotation[k] = 1f;
            handCF[k] = 1f;
        }

        /* initialize transformation variables */
        param = p;
//        param.transX = param.transY = 0;
        param.texScale = 1.0f;
        param.texTransX = param.texTransY = 0;
        lighting = true;
        this.handPars = handPars;
    }
    
    @Override
    public synchronized void onDrawFrame(GL10 gl) { 
        // display function
        gl.glClearColor (0, 0, 0, 1f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        GLU.gluLookAt(gl, param.eyeX, param.eyeY, param.eyeZ, /* eye */ 
                param.coa[0], param.coa[1], param.coa[2], /* focal point */
                0, 0, 1f); /* up */
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glPushMatrix();
        gl.glTranslatef(param.litePos[0], param.litePos[1], param.litePos[2]);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDir, 0);
        gl.glPopMatrix();

        if (lighting)
        	gl.glEnable(GL10.GL_LIGHTING);
        else
        	gl.glDisable (GL10.GL_LIGHTING);
        alley.bind();
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glPushMatrix();
		gl.glScalef (6f, 4f, 1f);

		/* transform the texture */
		gl.glMatrixMode(GL10.GL_TEXTURE);

		/* apply the tex coord transform only to the triangle */
		gl.glPushMatrix();
		{ /* this curly brace is optional, just to force autoindentation */
			gl.glLoadIdentity();
			gl.glTranslatef(-param.texTransX / param.texScale, param.texTransY
					/ param.texScale, 0);
			gl.glScalef(1 / param.texScale, -1f / param.texScale, 1f);
			box.draw();
		}
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glPopMatrix();
        
		alley.unbind();
        
        ball.bind();
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
        gl.glColor4f(1f, 1f, 1f, 1f);

    	/* limit the rolling motion when the ball hits the border */
        if (isLandscape) {
            if (param.sphTrX < -3 + SPH_SIZE) {
            	param.sphTrX = -3 + SPH_SIZE;
            	param.roll_x = 0; /* stop rolling */
            }
            else if (param.sphTrX > 3 - SPH_SIZE) {
            	param.sphTrX = 3 - SPH_SIZE;
            	param.roll_x = 0;
            }
            if (param.sphTrY < -2f + SPH_SIZE) {
            	param.sphTrY = -2f + SPH_SIZE;
            	param.roll_y = 0;
            }
            else if (param.sphTrY > 2f - SPH_SIZE) {
            	param.sphTrY = 2f - SPH_SIZE;
            	param.roll_y = 0;
            }
        }
        else {
            if (param.sphTrY < -3 + SPH_SIZE) {
            	param.sphTrY = -3 + SPH_SIZE;
            	param.roll_y = 0;
            }
            else if (param.sphTrX > 3 - SPH_SIZE) {
            	param.sphTrY = 3 - SPH_SIZE;
            	param.roll_y = 0;
            }
            if (param.sphTrX < -2 + SPH_SIZE) {
            	param.sphTrX = -2f + SPH_SIZE;
            	param.roll_x = 0;
            }
            else if (param.sphTrX > 2f - SPH_SIZE) {
            	param.sphTrX = 2f - SPH_SIZE;
            	param.roll_x = 0;
            }
        }
        Log.d(TAG, "Sphere (" + param.sphTrX + "," + param.sphTrY + ")  Droid ("
                + param.droid_x + "," + param.droid_y + ")" +
            " dx=" + Math.abs(param.sphTrX - param.droid_x) + " dy=" +
                Math.abs(param.sphTrY - param.droid_y));
        if (Math.abs(param.sphTrX - param.droid_x) < 1.2 &&
                Math.abs(param.sphTrY - param.droid_y) < 1.2)
        {
            param.roll_x = param.roll_y = 0.0f;
        }
        
        /* (roll_x, roll_y) is the direction where the sphere is supposed to roll */
        double rollDist = Math.sqrt(param.roll_x * param.roll_x + 
        		param.roll_y * param.roll_y);
        double rollAng = (rollDist * 180.0) / (Math.PI * SPH_SIZE);
        
        if (rollDist > 1E-6) { /* avoid divide by zero */
			gl.glPushMatrix();
			/*
			 * the following the new roll rotation (expressed in the fixed
			 * global coordinate) is superimposed on the current sphere (total)
			 * rotation. This technique was based on the snippet of OpenGL code
			 * posted at
			 * http://www.gamedev.net/topic/190307-solved-code-for-a-rolling-ball
			 * posted on (Nov 12, 2003).
			 */
			gl.glLoadIdentity();
			/* axis of rotation is perpendicular to the roll direction */
			gl.glRotatef((float) rollAng, -param.roll_y / (float) rollDist,
					param.roll_x / (float) rollDist, 0);
			gl.glMultMatrixf(sphRotation, 0);
			((GL11) gl).glGetFloatv(GL11.GL_MODELVIEW_MATRIX, sphRotation, 0);
			gl.glPopMatrix();
        }

        gl.glPushMatrix();
        gl.glTranslatef(param.sphTrX, param.sphTrY, 0.5f);
        gl.glMultMatrixf(sphRotation, 0);
        gl.glScalef(SPH_SIZE, SPH_SIZE, SPH_SIZE);
        sphere.draw();
        gl.glPopMatrix();
        ball.unbind();

        skin.bind();
        gl.glPushMatrix();
        gl.glLoadMatrixf(handCF, 0);
        if(movingHandForward) {
            if(handPosY <= MAX_HAND_POS) {
                if(anim) {
                    handPosY += .005f;
                    //handAngle -= .375f;
                    gl.glMultMatrixf(MatrixHelper.getTranslationMatrix(0, .005f, 0), 0);
                    gl.glMultMatrixf(MatrixHelper.getRotationMatrix(-.375f, 1, 0, 0), 0);
                }
                /*gl.glTranslatef( 0, handPosY, 0);
                gl.glRotatef(handAngle, 1, 0, 0);*/
            } else {
                movingHandForward = false;
            }
        } else if (handPosY >= MIN_HAND_POS) {
            if(anim){
                handPosY -= .005f;
                //handAngle += .375f;
                gl.glMultMatrixf(MatrixHelper.getTranslationMatrix(0, -.005f, 0), 0);
                gl.glMultMatrixf(MatrixHelper.getRotationMatrix(.375f, 1, 0, 0), 0);
            }
            //gl.glTranslatef(0, handPosY, 0);
            //gl.glRotatef(handAngle, 1, 0, 0);
        } else {
            movingHandForward = true;
        }

        ((GL11) gl).glGetFloatv(GL11.GL_MODELVIEW_MATRIX, handCF, 0);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslatef(-3f, 0f, 2f);
        gl.glRotatef(-90f, 0f, 0f, 1f);
        gl.glRotatef(180, 1f, 0f, 0f);
        gl.glScalef (4f, 4f, 4f);

        gl.glMultMatrixf(handCF, 0);
        hand.draw();
        gl.glPopMatrix();
        skin.unbind();

        metal.bind();
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslatef(0, 1f, 0);
        for(int i = 0; i < 4; i++) {
            gl.glPushMatrix();
            gl.glTranslatef(randomX[i], 0, randomY[i]);
            gl.glRotatef(randomXrot[i], 1, 0, 0);
            gl.glRotatef(randomYrot[i], 0, 1, 0);
            gl.glRotatef(randomZrot[i], 0, 0, 1);
            pin.draw();
            gl.glPopMatrix();
        }
        metal.unbind();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        scrWidth = width;
        scrHeight = height;
        // resize function
//        Log.i(TAG, "onSurfaceChange() " + width + "x" + height);
        
        gl.glViewport (0, 0, width, height);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        
        /* the following choice of orthographic projection parameters
         * guarantee that the shorter side is at least 2 units long
         */
        isLandscape = width > height;
        ratio = (float) width / height;
        GLU.gluPerspective(gl, 60, ratio, 0.5f, 10.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        Log.i(TAG, "onSurfaceCreated() ");

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glShadeModel(GL10.GL_SMOOTH);

        /* Light settings */
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmb, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDif, 0);
//        gl.glLightfv(GL_LIGHT0, GL_POSITION, lightPos, 0);
        gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 20);
        gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 1);
        
        gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, lightAmb,0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT_AND_DIFFUSE, materialAmb, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, materialSpe, 0);
        gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, 10);
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        /* can't do this in the constructor */
        logo = new Texture (mCtx, R.drawable.cis_logo);
        wood = new Texture (mCtx, R.drawable.wood);
        rubber = new Texture (mCtx, R.drawable.rubber);
        alley = new Texture(mCtx, R.drawable.alley);
        skin = new Texture(mCtx, R.drawable.hand);
        metal = new Texture(mCtx, R.drawable.metal);
        ball = new Texture(mCtx, R.drawable.ball);

        randomX = new float[numPins];
        randomY = new float[numPins];
        randomXrot = new float[numPins];
        randomYrot = new float[numPins];
        randomZrot = new float[numPins];

        movePins();
    }

    public void movePins() {
        for(int i = 0; i < numPins; i++) {
            randomX[i] = ((float)Math.random() * 4) - 2;
            randomY[i] = ((float)Math.random() * 4) - 2;
            randomXrot[i] = ((float)Math.random() * 200) - 100;
            randomYrot[i] = ((float)Math.random() * 200) - 100;
            randomZrot[i] = ((float)Math.random() * 200) - 100;
        }
    }


    public void doSwipe(MotionEvent ev, int which)
    {
        switch (ev.getAction() & MotionEvent.ACTION_MASK)
        {
        case MotionEvent.ACTION_DOWN:
            prevX = ev.getX();
            prevY = ev.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            float tx = 2 * (isLandscape ? ratio : 1.0f) * (ev.getX() - prevX) / scrWidth;
            float ty = 2 * (isLandscape ? 1.0f : ratio) * (prevY - ev.getY()) / scrHeight;
            switch (which) {
            case R.id.movelight:
                param.litePos[0] += tx;
                param.litePos[1] += ty;
                break;
            case R.id.movetex:
                param.texTransX += tx;
                param.texTransY += ty;
                break;
            case R.id.moveball:
                param.sphTrX += tx; 
                param.sphTrY += ty;
                param.roll_x = tx;
                param.roll_y = ty;
                break;
            case R.id.moveeye:
                param.eyeX += tx;
                param.eyeY += ty;
                break;
            case R.id.movefocus:
                param.coa[0] += tx;
                param.coa[1] += ty;
            }
            prevX = ev.getX();
            prevY = ev.getY();
            break;
        case MotionEvent.ACTION_UP:
        }
    }
    
    public void doPinch (MotionEvent ev, int which)
    {
        /* two fingers are pressed */
        float dx, dy, currDist;
//        StringBuilder sb = new StringBuilder();
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_POINTER_DOWN:
            switch (which) {
            case R.id.movetex:
                dx = (ev.getX(0) - ev.getX(1)) / scrWidth;
                dy = (ev.getY(0) - ev.getY(1)) / scrHeight;
                prevDist = (float) Math.sqrt(dx * dx + dy * dy);
                isPinching = true;
                break;
            case R.id.moveeye:
            case R.id.movelight:
            case R.id.movefocus:
                prevX = ev.getX(0);
                prevY = ev.getY(0);
            }
            break;
        case MotionEvent.ACTION_POINTER_UP:
            isPinching = false;
//            sb.append("POINTER UP");
            break;
        case MotionEvent.ACTION_MOVE:
            float tx = (ev.getX(0) - prevX) / scrWidth;
            float ty = (prevY - ev.getY(0)) / scrHeight;
            switch (which) {
            case R.id.movetex:
                if (isPinching) {
                    dx = (ev.getX(0) - ev.getX(1)) / scrWidth;
                    dy = (ev.getY(0) - ev.getY(1)) / scrHeight;
                    currDist = (float) Math.sqrt(dx * dx + dy * dy);
                    param.texScale += currDist - prevDist;
                    prevDist = currDist;
                }
                break;
            case R.id.moveeye:
                param.eyeZ += 2 * (tx + ty);
                break;
            case R.id.movefocus:
                param.coa[2] += 2 * (tx + ty);
                break;
            case R.id.movelight:
                param.litePos[2] += 2 * (tx + ty);
                break;
            }
            prevX = ev.getX(0);
            prevY = ev.getY(0);
            break;
        default:
//            sb.append("OTHER EVENT: " + ev.getAction() + " ");
            break;
        }
//        Log.d(TAG, sb.toString());
    	
    }

    private long tstamp;
    
    void doTilt (float[] grav, long timestamp, int orient)
    {
//    	Log.i(TAG, String.format("Gravity %3.0f %3.0f %3.0f", grav[0], grav[1], grav[2]));
        float len = grav[0] * grav[0] + grav[1] * grav[1];
        if (len >= 10.0) {
        	param.tiltZRot = (float) (Math.atan2(grav[1], grav[0]) * 180.0 / Math.PI);

            switch (orient) {
            case Surface.ROTATION_0:
            	param.tiltZRot = 90 - param.tiltZRot;
                break;
            case Surface.ROTATION_90:
            	param.tiltZRot = -param.tiltZRot;
                break;
            case Surface.ROTATION_180:
            	param.tiltZRot = -param.tiltZRot - 90;
                break;
            case Surface.ROTATION_270:
            	param.tiltZRot = 180 - param.tiltZRot;
            }
        }
        len = grav[1]*grav[1] + grav[2]*grav[2];
        if (len  > 1) {
        	param.tiltXRot = (float)(Math.atan2(grav[2], grav[1]) * 180.0 / Math.PI);
        }
        float gravity_x = 0; 
        float gravity_y = 0;
        
        switch (orient) {
        case Surface.ROTATION_0:
            gravity_x = -grav[0];
            gravity_y = -grav[1];
            break;
        case Surface.ROTATION_90:
            gravity_x = grav[1];
            gravity_y = -grav[0];
            break;
        case Surface.ROTATION_180:
            gravity_x = grav[0];
            gravity_y = grav[1];
            break;
        case Surface.ROTATION_270:
            gravity_x = -grav[1];
            gravity_y = grav[0];
            break;
        }
        if (tstamp != 0) {
            /* event time stamp is in nano second, we want to convert 
             * it to second */
            final float dt = (timestamp - tstamp) / 1e9f;
            param.sphTrX += 5 * gravity_x * dt * dt; /* the total translation */
            param.sphTrY += 5 * gravity_y * dt * dt;
            param.roll_x = 5 * gravity_x * dt * dt;
            param.roll_y = 5 * gravity_y * dt * dt;
        }
        tstamp = timestamp;
    	
    }

    void setLighting (boolean onOff)
    {
    	lighting = onOff;
    }
    
    void setAnimation (boolean flag)
    {
        anim = flag;
    }
}

