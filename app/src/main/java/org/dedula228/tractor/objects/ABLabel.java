package org.dedula228.tractor.objects;

import android.opengl.Matrix;
import org.dedula228.tractor.Core;
import org.dedula228.tractor.GLObject;
import org.dedula228.tractor.R;
import org.dedula228.tractor.Vec3;
import org.dedula228.tractor.util.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_TEXTURE_2D;

public class ABLabel extends GLObject {

    private final int textureId;
    public Vec3 position;
    float lifeTime = 0;


    final int dataPerVertex = 3 + 3 + 2, maxVerticesCount = 4;
    protected FloatBuffer vertexBuffer;
    protected ShortBuffer indexBuffer;

    public ABLabel(Core core, String textureName, Vec3 position, float lifeTime) {
        super(core, R.raw.plane_vertex_shader, R.raw.plane_fragment_shader);
        this.lifeTime = lifeTime;
        vertexBuffer = Utils.floatBuffer(maxVerticesCount * dataPerVertex);
        vertexBuffer.put(new float[] {
                1, 1, 0, 0, 0, -1, 1, 0,
                -1, 1, 0, 0, 0, -1, 0, 0,
                1,-1, 0, 0, 0, -1, 1, 1,
                -1,-1, 0, 0, 0, -1, 0, 1,
        });

        indexBuffer = Utils.shortBuffer(maxVerticesCount);
        indexBuffer.put(new short[] {
                0, 1, 2, 3,
        });

        this.position = position;

        textureId = Utils.loadTexture(context, textureName);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(lifeTime != 0) {
            lifeTime -= delta;
            if (lifeTime <= 0)
                core.objects.remove(this);
        }
    }

    @Override
    public void render() {
        super.render();

        int uTexture = glGetUniformLocation(shader, "u_Texture");
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glUniform1i(uTexture, 0);

        int uLightPos = glGetUniformLocation(shader, "u_LightPos");
        glUniform3f(uLightPos, core.lightPos.x, core.lightPos.y, core.lightPos.z);

        int uMVMatrix = glGetUniformLocation(shader, "u_MVMatrix");
        float[] mMVPMatrix = new float[16];
        float[] mModelMatrix = new float[16];
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, position.x, position.y, position.z);
        Matrix.rotateM(mModelMatrix, 0, Utils.computeHeading(position, core.camPos), 0, 1, 0);
        Matrix.scaleM(mModelMatrix, 0, 3, 3, 3);
        Matrix.translateM(mModelMatrix, 0, 0, 1.7f, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, core.mViewMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(uMVMatrix, 1, false, mMVPMatrix, 0);

        int uMVPMatrix = glGetUniformLocation(shader, "u_MVPMatrix");
        Matrix.multiplyMM(mMVPMatrix, 0, core.mProjectionMatrix, 0, mMVPMatrix, 0);
        glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        int aPosition = glGetAttribLocation(shader, "a_Position");
        vertexBuffer.position(0);
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, dataPerVertex * 4, vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        int aNormal = glGetAttribLocation(shader, "a_Normal");
        vertexBuffer.position(3);
        glVertexAttribPointer(aNormal, 3, GL_FLOAT, false, dataPerVertex * 4, vertexBuffer);
        glEnableVertexAttribArray(aNormal);

        int aUV = glGetAttribLocation(shader, "a_UV");
        vertexBuffer.position(6);
        glVertexAttribPointer(aUV, 2, GL_FLOAT, false, dataPerVertex * 4, vertexBuffer);
        glEnableVertexAttribArray(aUV);

        indexBuffer.position(0);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDrawElements(GL_TRIANGLE_STRIP, indexBuffer.capacity(), GL_UNSIGNED_SHORT, indexBuffer);
        glDisable(GL_BLEND);

        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
