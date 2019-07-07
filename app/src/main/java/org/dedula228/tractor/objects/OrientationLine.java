package org.dedula228.tractor.objects;

import android.opengl.Matrix;
import org.dedula228.tractor.Core;
import org.dedula228.tractor.GLObject;
import org.dedula228.tractor.R;
import org.dedula228.tractor.Vec3;
import org.dedula228.tractor.util.Utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;


public class OrientationLine extends GLObject {
    private FloatBuffer vertexBuffer;
    private final int dataPerVertex = 3;
    private final float size = 1900;
    private final int LENGTH = 65536;
    private float width;

    public boolean curveMode = true;
    public Vec3 pointA, pointB;
    private List<Vec3> curve = new ArrayList<>();

    float[] mModelMatrix = new float[16];
    public OrientationLine(Core core) {
        super(core, R.raw.trail_vertex_shader, R.raw.trail_fragment_shader);

        vertexBuffer = Utils.floatBuffer(3 * LENGTH * dataPerVertex);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(pointB == null) {
            if(pointA == null) {
                if(!curve.isEmpty()) {
                    curve.clear();
                }
            } else {
                if (curveMode && (curve.isEmpty() || core.tractor.position.distance(curve.get(curve.size() - 1)) > 0.5f))
                    curve.add(core.tractor.position);
            }
            return;
        }

        width = core.tractor.points[1].distance(core.tractor.points[2]);

        float dZ = pointB.z - pointA.z;
        float dX = pointB.x - pointA.x;

        if(Math.abs(dX) > Math.abs(dZ)) {
            float k = dZ / dX;
            float b = this.pointA.z - k*this.pointA.x;

            float s = (dX > 0 ? size : -size);
            pointA.x = core.tractor.position.x - s;
            pointA.z = k*pointA.x + b;
            pointB.x = core.tractor.position.x + s;
            pointB.z = k*pointB.x + b;
        } else {
            float k = dX / dZ;
            float b = this.pointA.x - k*this.pointA.z;

            float s = (dZ > 0 ? size : -size);
            pointA.z = core.tractor.position.z - s;
            pointA.x = k*pointA.z + b;
            pointB.z = core.tractor.position.z + s;
            pointB.x = k*pointB.z + b;
        }

        Vec3 basis = Utils.getPerpendicular(pointA, pointB, core.tractor.position);
        basis = basis.setLength(width * (int)(basis.len() / width));
        float offsetX = basis.x;
        float offsetZ = basis.z;

        vertexBuffer.clear();
        vertexBuffer.position(0);

        vertexBuffer.put(new float[]{
                pointA.x + offsetX, pointA.y, pointA.z + offsetZ
        });
        for(Vec3 v : curve) {
            vertexBuffer.put(new float[]{
                    v.x + offsetX, v.y, v.z + offsetZ
            });
        }
        vertexBuffer.put(new float[]{
                pointB.x + offsetX, pointB.y, pointB.z + offsetZ
        });

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix,0, 0, 2.505f, 0);
    }

    @Override
    public void render() {
        super.render();
        if(pointA == null || pointB == null) return;

        int uColor = glGetUniformLocation(shader, "u_Color");
        glUniform4f(uColor, 1, 0, 0, 1.5f);

        int aPosition = glGetAttribLocation(shader, "a_Position");
        vertexBuffer.position(0);
        glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, 0, vertexBuffer);
        glEnableVertexAttribArray(aPosition);

        glLineWidth(10);
        Vec3 normal = pointA.sub(pointB).normalize().cross(new Vec3(0, -1, 0)).scl(width);
        Matrix.translateM(mModelMatrix, 0, -normal.x, -normal.y, -normal.z);
        for(int i = 0; i < 3; i++) {
            int uMVPMatrix = glGetUniformLocation(shader, "u_MVPMatrix");
            float[] mMVPMatrix = new float[16];
            Matrix.multiplyMM(mMVPMatrix, 0, core.mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, core.mProjectionMatrix, 0, mMVPMatrix, 0);
            glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

            glDrawArrays(GL_LINE_STRIP, 0, 2 + curve.size());
            Matrix.translateM(mModelMatrix, 0, normal.x, normal.y, normal.z);
        }
    }
}
