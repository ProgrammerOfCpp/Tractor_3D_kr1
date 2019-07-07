package org.dedula228.tractor.util;

import android.content.Context;
import android.opengl.Matrix;
import org.dedula228.tractor.Vec3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.*;

import static android.opengl.GLES20.*;

public class OBJModel {
    public Vec3 scale = new Vec3(1, 1, 1);
    class Material {
        int map_Kd = 0;
        float d = 1;
        Vec3 Ks = new Vec3();
        Vec3 Kd = new Vec3();
        Vec3 Ka = new Vec3();
    }

    class ModelPart {
        int dataPerVertex;
        FloatBuffer vertexBuffer = null;
        Material material;


        public void addPoint(Face face, int idx) {
            vertexBuffer.put(face.positions.get(idx * 3 + 0));
            vertexBuffer.put(face.positions.get(idx * 3 + 1));
            vertexBuffer.put(face.positions.get(idx * 3 + 2));

            vertexBuffer.put(face.normals.get(idx * 3 + 0));
            vertexBuffer.put(face.normals.get(idx * 3 + 1));
            vertexBuffer.put(face.normals.get(idx * 3 + 2));

            if(dataPerVertex == 8) {
                vertexBuffer.put(face.textures.get(idx * 2 + 0));
                vertexBuffer.put(1 - face.textures.get(idx * 2 + 1)); // y-invertion
            }
        }
    }

    Vector<ModelPart> parts = new Vector<>();

    class Face {
        int size;
        String textureName = "";
        Vector<Float> positions = new Vector<>();
        Vector<Float> textures = new Vector<>();
        Vector<Float> normals = new Vector<>();
    }

    Map<String, Material> materials = new HashMap<>();
    String usemtl = "";
    Vector<Float> v = new Vector<>();
    Vector<Float> vt = new Vector<>();
    Vector<Float> vn = new Vector<>();
    Vector<Face> faces = new Vector<>();

    int dataPerVertex, faceSize;

    public OBJModel(Context context, String filename) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            while((line = reader.readLine()) != null) {
                String[] data = line.split(" ");
                List<String> stringList = new ArrayList<>();
                for(int i = 0; i < data.length; i++)
                    if(!data[i].isEmpty())
                        stringList.add(data[i]);
                if(stringList.isEmpty())
                    continue;
                data = new String[stringList.size()];
                for(int i = 0; i < data.length; i++)
                    data[i] = stringList.get(i);
                switch (data[0]) {
                    case "v":
                        v.add(Float.parseFloat(data[1]));
                        v.add(Float.parseFloat(data[2]));
                        v.add(Float.parseFloat(data[3]));
                        break;
                    case "vt":
                        vt.add(Float.parseFloat(data[1]));
                        vt.add(Float.parseFloat(data[2]));
                        break;
                    case "vn":
                        vn.add(Float.parseFloat(data[1]));
                        vn.add(Float.parseFloat(data[2]));
                        vn.add(Float.parseFloat(data[3]));
                        break;
                    case "f":
                        Face face = new Face();
                        face.size = data.length - 1;
                        // If face size changed
                        if(face.size != faceSize && !parts.isEmpty()) {
                            finishModelPart();
                        }
                        faceSize = face.size;

                        for(int i = 1; i < data.length; i++) {
                            String[] pointData = data[i].split("/");

                            // If data per vertex changed
                            int newDataPerVertex = (pointData[1].isEmpty() ? 6 : 8);
                            if(dataPerVertex != newDataPerVertex && !parts.isEmpty()) {
                                finishModelPart();
                            }
                            dataPerVertex = newDataPerVertex;

                            face.positions.add(v.get((Integer.parseInt(pointData[0]) - 1) * 3 + 0));
                            face.positions.add(v.get((Integer.parseInt(pointData[0]) - 1) * 3 + 1));
                            face.positions.add(v.get((Integer.parseInt(pointData[0]) - 1) * 3 + 2));

                            face.normals.add(vn.get((Integer.parseInt(pointData[2]) - 1) * 3 + 0));
                            face.normals.add(vn.get((Integer.parseInt(pointData[2]) - 1) * 3 + 1));
                            face.normals.add(vn.get((Integer.parseInt(pointData[2]) - 1) * 3 + 2));

                            if(dataPerVertex == 8) {
                                face.textures.add(vt.get((Integer.parseInt(pointData[1]) - 1) * 2 + 0));
                                face.textures.add(vt.get((Integer.parseInt(pointData[1]) - 1) * 2 + 1));
                            }
                        }
                        faces.add(face);
                        break;
                    case "mtllib":
                        loadMaterials(context, data[1]);
                        usemtl = materials.entrySet().iterator().next().getKey();
                        break;
                    case "usemtl":
                        if(!usemtl.equals(data[1]) && !usemtl.isEmpty()) {
                            finishModelPart();
                        }
                        usemtl = data[1];
                        break;
                }
            }
            finishModelPart();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMaterials(Context context, String mtllib) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(mtllib)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String matName = "";
        String line = null;
        try {
            while((line = reader.readLine()) != null) {
                String[] data = line.split(" ");
                switch (data[0]) {
                    case "newmtl":
                        matName = data[1];
                        materials.put(matName, new Material());
                        break;
                    case "Ks":
                        materials.get(matName).Ks = new Vec3(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3]));
                        break;
                    case "Kd":
                        materials.get(matName).Kd = new Vec3(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3]));
                        break;
                    case "Ka":
                        materials.get(matName).Ka = new Vec3(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3]));
                        break;
                    case "map_Kd":
                        materials.get(matName).map_Kd = Utils.loadTexture(context, data[1]);
                        break;
                    case "d":
                        materials.get(matName).d = Float.parseFloat(data[1]);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finishModelPart() {
        if(faces.isEmpty()) return;
        ModelPart part = new ModelPart();
        part.material = materials.get(usemtl);
        part.dataPerVertex = dataPerVertex;
        part.vertexBuffer = Utils.floatBuffer(faces.size() * (faces.get(0).size - 2) * 3 * part.dataPerVertex);

        for(Face face : faces) {
            for(int point = 1; point < face.size - 1; point++) {
                part.addPoint(face, 0);
                part.addPoint(face, point);
                part.addPoint(face, point + 1);
            }
        }
        parts.add(part);

        faces.clear();
    }

    public void render(int shader, float[] mModelMatrix, float[] mViewMatrix, float[] mProjectionMatrix, Vec3 lightPos, boolean useBlending) {
        int uScale = glGetUniformLocation(shader, "u_Scale");
        glUniform3f(uScale, scale.x, scale.y, scale.z);

        int uLightPos = glGetUniformLocation(shader, "u_LightPos");
        glUniform3f(uLightPos, lightPos.x, lightPos.y, lightPos.z);

        int uMVMatrix = glGetUniformLocation(shader, "u_MVMatrix");
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        glUniformMatrix4fv(uMVMatrix, 1, false, mMVPMatrix, 0);

        int uMVPMatrix = glGetUniformLocation(shader, "u_MVPMatrix");
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        glUniformMatrix4fv(uMVPMatrix, 1, false, mMVPMatrix, 0);

        for(int i = 0; i < parts.size(); i++) {
            ModelPart part = parts.get(i);

            int aPosition = glGetAttribLocation(shader, "a_Position");
            part.vertexBuffer.position(0);
            glVertexAttribPointer(aPosition, 3, GL_FLOAT, false, part.dataPerVertex * 4, part.vertexBuffer);
            glEnableVertexAttribArray(aPosition);

            int aNormal = glGetAttribLocation(shader, "a_Normal");
            part.vertexBuffer.position(3);
            glVertexAttribPointer(aNormal, 3, GL_FLOAT, false, part.dataPerVertex * 4, part.vertexBuffer);
            glEnableVertexAttribArray(aNormal);

            if(part.dataPerVertex == 8) {
                int aUV = glGetAttribLocation(shader, "a_UV");
                part.vertexBuffer.position(6);
                glVertexAttribPointer(aUV, 2, GL_FLOAT, false, part.dataPerVertex * 4, part.vertexBuffer);
                glEnableVertexAttribArray(aUV);
            }

            Material mat = part.material;
            if(mat.map_Kd != 0) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE0, mat.map_Kd);
                glUniform1f(glGetUniformLocation(shader, "useTexture"), 1);
                glUniform1i(glGetUniformLocation(shader, "u_Texture"), mat.map_Kd);
            } else {
                glUniform1f(glGetUniformLocation(shader, "useTexture"), 0);
            }

            glUniform1f(glGetUniformLocation(shader, "d"), mat.d);
            glUniform4f(glGetUniformLocation(shader, "u_Diffuse"), mat.Kd.x, mat.Kd.y, mat.Kd.z, 1);
            glUniform4f(glGetUniformLocation(shader, "u_Ambient"), mat.Ka.x, mat.Ka.y, mat.Ka.z, 1);
            glUniform4f(glGetUniformLocation(shader, "u_Specular"), mat.Ks.x, mat.Ks.y, mat.Ks.z, 1);

            int size = part.vertexBuffer.capacity() / part.dataPerVertex;

            glDisable(GL_CULL_FACE);
            if(useBlending) {
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }
            glDrawArrays(GL_TRIANGLES, 0, size);
            if(useBlending) {
                glDisable(GL_BLEND);
            }
            glEnable(GL_CULL_FACE);

            if(mat.map_Kd != 0) {
                glBindTexture(GL_TEXTURE0, 0);
            }
        }
    }
}
