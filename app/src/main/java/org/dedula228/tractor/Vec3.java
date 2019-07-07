package org.dedula228.tractor;

import android.opengl.Matrix;

public class Vec3 {
    public static final float deg2rad = (float)Math.PI / 180;

    public float x, y, z;
    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float[] vec) {
        this.x = vec[0];
        this.y = vec[1];
        this.z = vec[2];
    }

    public Vec3(float len) {
        this.x = 0;
        this.y = 0;
        this.z = len;
    }

    public Vec3(Vec3 vec3) {
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
    }

    public Vec3 scl(float scalar) {
        return new Vec3(x * scalar, y * scalar, z * scalar);
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public float dot(Vec3 v) {
        return (this.x*v.x + this.y*v.y + this.z*v.z);
    }

    public float angle(Vec3 v) {
        Vec3 a = this.normalize();
        Vec3 b = v.normalize();
        if(a.equals(b)) return 0;
        float res = (float)Math.toDegrees(Math.acos(a.dot(b)));
        return res;
    }

    public float distance(Vec3 v) {
        return v.sub(this).len();
    }

    public Vec3 center(Vec3 v) {
        return new Vec3((x + v.x) / 2, (y + v.y) / 2, (z + v.z) / 2);
    }

    public Vec3 normalize() {
        return setLength(1);
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    public Vec3 sub(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }

    public float len() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3 setLength(float len) {
        float l = len();
        if(l == 0)
            return new Vec3(0, 0, len);
        return new Vec3(x * len / l, y * len / l, z * len / l);
    }

    public Vec3 rotY(float angle) {
        float len = len();
        return new Vec3((float) Math.sin((double) angle * deg2rad) * len, y, (float) Math.cos((double) angle * deg2rad) * len);
    }

    public float[] toFloatArray() {
        return new float[]{ x, y, z, 1 };
    }

    public Vec3 mul(float[] matrix) {
        float[] vec = this.toFloatArray();
        Matrix.multiplyMV(vec, 0, matrix, 0, vec, 0);
        return new Vec3(vec);
    }

    @Override
    public String toString() {
        return ("x " + x + ", y " + y + ", z " + z);
    }

    public boolean equals(Vec3 o) {
        float prec = 0;
        return (Math.abs(x - o.x) < prec && Math.abs(y - o.y) < prec && Math.abs(z - o.z) < prec);
    }
}
