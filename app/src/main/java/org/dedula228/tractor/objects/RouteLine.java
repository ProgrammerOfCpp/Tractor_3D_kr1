package org.dedula228.tractor.objects;

import android.opengl.Matrix;
import org.dedula228.tractor.Core;
import org.dedula228.tractor.GLObject;
import org.dedula228.tractor.R;
import org.dedula228.tractor.Vec3;
import org.dedula228.tractor.util.Utils;

import java.nio.FloatBuffer;
import java.util.List;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.glDrawArrays;

public class RouteLine extends GLObject{
    FloatBuffer vertexBuffer;
    private final int dataPerVertex = 3;

    float[] mModelMatrix = new float[16];
    public RouteLine(Core core, List<Vec3> traectory) {
        super(core, R.raw.trail_vertex_shader, R.raw.trail_fragment_shader);

        vertexBuffer = Utils.floatBuffer(traectory.size() * dataPerVertex);
        vertexBuffer.position(0);
        for(int i = 0; i < traectory.size(); i++) {
            vertexBuffer.put(traectory.get(i).x);
            vertexBuffer.put(traectory.get(i).y);
            vertexBuffer.put(traectory.get(i).z);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0, 0, 0.703f, 0);
    }

    @Override
    public void render() {
        super.render();

        int uColor = glGetUniformLocation(shader, "u_Color");
        glUniform4f(uColor, 1, 0, 0, 1f);

        int uMVPMatrix = glGetUniformLocation(shader, "u_MVPMatrix");
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, core.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, core.mProjectionMatrix, 0, mMVPMatrix, 0);
        glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        int aPosition = glGetAttribLocation(shader, "a_Position");
        vertexBuffer.position(0);
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        glLineWidth(1);
        glDrawArrays(GL_LINE_STRIP, 0, vertexBuffer.capacity() / dataPerVertex);
    }
}
