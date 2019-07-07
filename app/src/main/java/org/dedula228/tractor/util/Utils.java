package org.dedula228.tractor.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import static android.opengl.GLES20.*;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import org.dedula228.tractor.Vec3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;

public class Utils {
    public static int round(double d){
        double dAbs = Math.abs(d);
        int i = (int) dAbs;
        double result = dAbs - (double) i;
        if(result<0.5){
            return d<0 ? -i : i;
        }else{
            return d<0 ? -(i+1) : i+1;
        }
    }

    public static int loadShader(Context context, int vertexShaderRawId, int fragmentShaderRawId) {
        final int programId = glCreateProgram();
        if (programId == 0) {
            return 0;
        }

        int vertexShaderId = createShader(GLES20.GL_VERTEX_SHADER, readTextFromRaw(context, vertexShaderRawId));
        int fragmentShaderId = createShader(GLES20.GL_FRAGMENT_SHADER, readTextFromRaw(context, fragmentShaderRawId));
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {

            Log.e("LOL", "Error linking shader: " + GLES20.glGetProgramInfoLog(programId));
            glDeleteProgram(programId);
            return 0;
        }
        return programId;
    }

    public static int loadTexture(Context context, String filename) {
        final int[] textureIds = new int[1];
        glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(filename));
        } catch (IOException e) {
            glDeleteTextures(1, textureIds, 0);
            e.printStackTrace();
            return 0;
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap, GL_UNSIGNED_BYTE, 0);
        bitmap.recycle();
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    public static String readTextFromRaw(Context context, int resourceId) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = context.getResources().openRawResource(resourceId);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\r\n");
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Resources.NotFoundException nfex) {
            nfex.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static int createShader(int type, String shaderText) {
        final int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            return 0;
        }
        glShaderSource(shaderId, shaderText);
        glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e("LOL", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderId));
            glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }

    public static ShortBuffer shortBuffer(int elemsentsCount) {
        ShortBuffer buffer = ByteBuffer.allocateDirect(elemsentsCount * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        buffer.position(0);
        return buffer;
    }

    public static FloatBuffer floatBuffer(int elemsentsCount) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(elemsentsCount * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.position(0);
        return buffer;
    }

    public static float computeHeading(Vec3 a, Vec3 b) {
        float angle = (float)Math.toDegrees(Math.atan2(b.x - a.x, b.z - a.z));
        return clampAngle(angle);
    }

    public static float getArcLength(float a, float r) {
        return Math.abs(a * (float)Math.PI*r*r / 360);
    }

    public static float getArc(float arcLength, float r) {
        return Math.abs(arcLength * 360 / (float)(Math.PI*r*r));
    }

    public static float getDeltaAngle(float a, float b) {
        float r1, r2;
        if (a>b) { r1 = a-b; r2 = b-a+360; } else { r1 = b-a; r2=a-b+360; };
        return Math.abs((r1>r2) ? r2 : r1);
    }

    public static int getSpin(float from, float to) {
        if(from == to) return 0;
        if (to > from) {
            if(to - from < 180) {
                return  1;
            } else {
                return -1;
            }
        } else {
            if(from - to < 180) {
                return -1;
            } else {
                return  1;
            }
        }
    }

    public static float clampAngle(float angle) {
        if(angle < 0)
            angle += 360;
        angle = angle % 360;
        return angle;
    }

    public static float rotateTowards(float from, float to, float step) {
        step = Math.abs(step);
        float maxStep = getDeltaAngle(from, to);
        if(step > maxStep)
            step = maxStep;
        from += getSpin(from, to) * step;
        return clampAngle(from);
    }

    public static Vec3 moveTowards(Vec3 from, Vec3 to, float step) {
        Vec3 dist = to.sub(from);
        System.out.println("Move to " + dist.len());
        if(dist.len() == 0) {
            return new Vec3(from);
        }
        if(step > dist.len())
            step = dist.len();
        return new Vec3(from).add(dist.normalize().scl(step));
    }

    public static float dist(Vec3 A, Vec3 B, Vec3 C) {
        return ((B.z-A.z)*C.x - (B.x-A.x)*C.z + B.x*A.z - B.z*A.x) / (B.sub(A).len());
    }

    public static Vec3 getPerpendicular(Vec3 A, Vec3 B, Vec3 C) {
        Vec3 b = B.sub(A);
        Vec3 c = C.sub(A);

        float k = (b.x*c.x + b.z*c.z)/(b.x*b.x + b.z*b.z);
        float x = k*b.x;
        float z = k*b.z;

        return C.sub(new Vec3(x, 0, z).add(A));
    }
}