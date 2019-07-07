package org.dedula228.tractor.objects;

import android.opengl.Matrix;
import org.dedula228.tractor.*;
import org.dedula228.tractor.util.Utils;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.*;

public class Trail extends GLObject {

    short verticesCount = 0;
    final int dataPerVertex = 3, maxVerticesCount = 2730;
    protected FloatBuffer vertexBuffer;

    public Trail(Core core) {
        super(core, R.raw.trail_vertex_shader, R.raw.trail_fragment_shader);

        vertexBuffer = Utils.floatBuffer(maxVerticesCount * dataPerVertex);

        addQuad();
        addQuad();
    }

    public void addQuad() {
        if(verticesCount == maxVerticesCount) {
            correctPoints();
            core.trail = new Trail(core);
            return;
        }

        verticesCount += 2;
        correctPoints();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void correctPoints() {
        Vec3[] corners = core.tractor.getPoints();

        vertexBuffer.position((verticesCount - 2) * dataPerVertex);
        vertexBuffer.put(new float[] {
                corners[1].x, 0, corners[1].z,
                corners[2].x, 0, corners[2].z,
        });
    }

    @Override
    public void render() {
        super.render();

        int uColor = glGetUniformLocation(shader, "u_Color");
        glUniform4f(uColor, 0, 0, 1, 0.5f);

        int uMVPMatrix = glGetUniformLocation(shader, "u_MVPMatrix");
        float[] mModelMatrix = new float[16];
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0.001f, 0);
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, core.mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, core.mProjectionMatrix, 0, mMVPMatrix, 0);
        glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        int aPosition = glGetAttribLocation(shader, "a_Position");
        vertexBuffer.position(0);
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, verticesCount);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }
}
