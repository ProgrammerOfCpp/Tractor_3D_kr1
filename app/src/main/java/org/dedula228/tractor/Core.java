package org.dedula228.tractor;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import org.dedula228.tractor.objects.*;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class Core implements Renderer {
    public Context context;

    private long oldTime = SystemClock.uptimeMillis();

    public float[] mViewMatrix = new float[16];
    public float[] mProjectionMatrix = new float[16];

    public Vec3 camPos = new Vec3(0, 3, 3);
    public Vec3 camLookAtPos = new Vec3(0, 0, 0);

    public Vec3 lightPos = new Vec3(0, 100, 0);

    public List<GLObject> objects = new ArrayList<>();
    public Plane plane;
    public Tractor tractor;
    public Trail trail;
    public OrientationLine line;
    public ABLabel ALabel, BLabel;
    public RouteLine routeLine;

    public Core(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);

        plane = new Plane(this);
        tractor = new Tractor(this);
        line = new OrientationLine(this);
        ALabel = new ABLabel(this, "A.png", new Vec3(0, -10, 0), 0);
        BLabel = new ABLabel(this, "B.png", new Vec3(0, -10, 0), 0);
        new RouteLine(this, tractor.traectory);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);

        gluPerspective(60.0f, (float)width / height, 0.07f, 10000f);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        glClearColor(0, 0.5f, 1, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        objects.remove(tractor);
        objects.add(tractor);
        objects.remove(ALabel);
        objects.add(ALabel);
        objects.remove(BLabel);
        objects.add(BLabel);

        long newTime = SystemClock.uptimeMillis();
        float deltaTime = (newTime - oldTime) / 1000.0f;
        for(int i = 0; i < objects.size(); i++)
            objects.get(i).update(deltaTime);
        oldTime = newTime;

        Matrix.setLookAtM(mViewMatrix, 0, camPos.x, camPos.y, camPos.z, camLookAtPos.x, camLookAtPos.y, camLookAtPos.z, 0, 1, 0);

        for(int i = 0; i < objects.size(); i++) {
            glUseProgram(objects.get(i).shader);
            objects.get(i).render();
        }
    }

    void gluPerspective(float fovyInDegrees, float aspectRatio, float znear, float zfar)
    {
        float ymax = znear * (float)Math.tan((double)fovyInDegrees * Math.PI / 360.0);
        float xmax = ymax * aspectRatio;
        Matrix.frustumM(mProjectionMatrix, 0, -xmax, xmax, -ymax, ymax, znear, zfar);
    }
}