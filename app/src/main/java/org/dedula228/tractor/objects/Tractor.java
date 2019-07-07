package org.dedula228.tractor.objects;

import android.opengl.Matrix;
import org.dedula228.tractor.*;
import org.dedula228.tractor.util.Assets;
import org.dedula228.tractor.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tractor extends GLObject {
    public static float SCALE = 4.5f;
    public static final float SPEED = 4.1f*4;
    public static final double COORD_SCALE_X = 111195.08372419157;
    public static final double COORD_SCALE_Z = 69976.79714551783;
    public static final float FRONT_RAD = 1.5f, BACK_RAD = 3.0f;
    private double COORD_OFFSET_X = 0, COORD_OFFSET_Z = 0;
    private float[] COORD_ROT_MAT = new float[16];
    private int nac;

    public Vec3 position = new Vec3(0, 0, 0), frontWheels, backWheels;
    public Vec3 rotation = new Vec3(0, 0, 0);

    public List<Vec3> traectory = new ArrayList<>();
    int targetIdx = 0;

    float[] mModelMatrix = new float[16];

    Vec3[] points = {
            new Vec3(0, 0, -2),// center
            new Vec3(10.5f, 0, -6.22f), new Vec3(-10.5f, 0, -6.22f), // opriskivatel
            new Vec3(0.455f, 0.297f, 0.726f).scl(SCALE), new Vec3(-0.455f, 0.297f, 0.726f).scl(SCALE), // front wheels
            new Vec3(0.476f, 0.42f, -0.392f).scl(SCALE), new Vec3(-0.476f, 0.42f, -0.392f).scl(SCALE), // back wheels
    };
    Vec3[] wheelRots = { new Vec3(), new Vec3(), new Vec3(), new Vec3() };

    public Vec3 camera2Tractor = new Vec3(0, 10, 10);
    public float cameraRotation;
    float prevRotY = rotation.y;
    private boolean wasTrailOn = false;

    public Tractor(Core core) {
        super(core, R.raw.tractor_vertex_shader, R.raw.tractor_fragment_shader);


        for(int times = 0; times < ((MainActivity)context).fileStrings.size(); times++) {
            String[] tokens = ((MainActivity)context).fileStrings.get(times).split(";");
            final double TARGET_LATITUDE = Double.parseDouble(tokens[0]);
            final double TARGET_LONGITUDE = Double.parseDouble(tokens[1]);
            addTracetoryPoint(TARGET_LATITUDE, TARGET_LONGITUDE);
        }
        new RouteLine(core, traectory);


        setPosition(new Vec3(), points[0]);
        cameraRotation = rotation.y + 180;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (targetIdx < traectory.size())
            moveTraectory(delta);

        processTrail();

        float CAMERA_SPEED = 77f;
        core.camLookAtPos = new Vec3(this.position);
        core.camPos = position.add(new Vec3(0, camera2Tractor.y, camera2Tractor.z).rotY(cameraRotation = Utils.rotateTowards(cameraRotation, Utils.clampAngle(rotation.y + 180 + nac), CAMERA_SPEED * delta)).add(new Vec3(0, 0, 0)));
    }

    @Override
    public void render() {
        super.render();

        if (((MainActivity) context).zoomPlus.isPressed() && camera2Tractor.len()<101) camera2Tractor = camera2Tractor.setLength(camera2Tractor.len() + 1);
        if (((MainActivity) context).zoomMinus.isPressed() && camera2Tractor.len()>1) camera2Tractor = camera2Tractor.setLength(camera2Tractor.len() - 1);



        float[] bodyMatrix = Arrays.copyOf(mModelMatrix, 16);
        Matrix.scaleM(bodyMatrix, 0, SCALE, SCALE, SCALE);
        Assets.tractor.render(shader, bodyMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);

        float[] wheelMatrix;
        wheelMatrix = Arrays.copyOf(bodyMatrix, 16);
        Matrix.translateM(wheelMatrix, 0, -0.455f, 0.297f, 0.726f);
        Matrix.rotateM(wheelMatrix, 0, 0, 0, 1, 0);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[0].y, 0, 1, 0);
        Assets.wing.scale = new Vec3(1, 1, 1);
        Assets.wing.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[0].x, 1, 0, 0);
        Assets.frontWheel.scale = new Vec3(1, 1, 1);
        Assets.frontWheel.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);

        wheelMatrix = Arrays.copyOf(bodyMatrix, 16);
        Matrix.translateM(wheelMatrix, 0,  0.445f, 0.297f, 0.726f);
        Matrix.rotateM(wheelMatrix, 0, 0, 0, 1, 0);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[1].y, 0, 1, 0);
        Assets.wing.scale = new Vec3(-1, 1, 1);
        Assets.wing.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[1].x, 1, 0, 0);
        Assets.frontWheel.scale = new Vec3(-1, 1, 1);
        Assets.frontWheel.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);

        wheelMatrix = Arrays.copyOf(bodyMatrix, 16);
        Matrix.translateM(wheelMatrix, 0, -0.476f, 0.42f, -0.392f);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[2].y, 0, 1, 0);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[2].x, 1, 0, 0);
        Assets.backWheel.scale = new Vec3(1, 1, 1);
        Assets.backWheel.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);

        wheelMatrix = Arrays.copyOf(bodyMatrix, 16);
        Matrix.translateM(wheelMatrix, 0, 0.476f, 0.42f, -0.392f);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[3].y, 0, 1, 0);
        Matrix.rotateM(wheelMatrix, 0, wheelRots[3].x, 1, 0, 0);
        Assets.backWheel.scale = new Vec3(-1, 1, 1);
        Assets.backWheel.render(shader, wheelMatrix, core.mViewMatrix, core.mProjectionMatrix, core.lightPos, false);
    }

    private void moveTraectory(float delta) {
        float movement = delta * SPEED;

        /*while (movement > 0) {
            if(targetIdx >= traectory.size()) return;
            Vec3 target = traectory.get(targetIdx);
            if (target.equals(position) || (target.distance(position) < 3)) {
                nextTarget();
                continue;
            }

            float step;

            float heading = Utils.computeHeading(position, target);
            if(heading != rotation.y) {
                int spin = Utils.getSpin(rotation.y, heading);
                Vec3 A = new Vec3(target);
                Vec3 B = points[spin == 1 ? 5 : 6].mul(mModelMatrix);
                Vec3 C = points[spin == 1 ? 6 : 5].mul(mModelMatrix);
                Vec3 BA = A.sub(B);
                Vec3 BC = C.sub(B);
                double alpha = Math.asin(0.5f*BC.len() / B.distance(A));
                float angle = Math.abs(BA.angle(BC) - (180 - 90 - (float)Math.toDegrees(alpha)));

                step = Math.min(Utils.getArcLength(angle, position.distance(B)), movement);
                float theta = Utils.getArc(step, position.distance(B))*spin;
                rotate(theta);
                //setRotation(rotation.y + theta, points[0]);

                System.out.println("Alpha: " + alpha + ", delta: " + "" + ", theta: " + theta);

                processTrail();
            } else {
                step = Math.min(movement, target.distance(position));
                moveForward(step);
            }
            wheelRots[0].y = wheelRots[1].y = Utils.clampAngle(Utils.computeHeading(frontWheels, target) - rotation.y);
            movement -= step;
        }*/
        if(targetIdx >= traectory.size()) return;
        Vec3 target = traectory.get(targetIdx);
        if (target.equals(position) || (target.distance(position) < 3)) {
            targetIdx++;
            moveTraectory(delta);
            return;
        }

        moveForward(movement);
        float heading = Utils.computeHeading(position, target);
        float newRot = Utils.rotateTowards(rotation.y, heading, 360*delta);
        rotate(Utils.getDeltaAngle(rotation.y, newRot) * Utils.getSpin(rotation.y, newRot));

        wheelRots[0].y = wheelRots[1].y = Utils.computeHeading(frontWheels, target) - rotation.y;
    }

    public void setPosition(Vec3 position, Vec3 point) {
        this.position = new Vec3(position);
        Vec3 toCenter = point.scl(-1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, position.x, position.y, position.z);
        Matrix.rotateM(mModelMatrix, 0, rotation.y, 0, 1, 0);
        Matrix.translateM(mModelMatrix, 0, toCenter.x, toCenter.y, toCenter.z);

        refreshVectors();
    }

    public void setRotation(float angle, Vec3 point) {
        angle = Utils.clampAngle(angle);
        this.rotation.y = angle;
        Vec3 toCenter = point.scl(-1);
        Vec3 center = point.mul(mModelMatrix);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, center.x, center.y, center.z);
        Matrix.rotateM(mModelMatrix, 0, rotation.y, 0, 1, 0);
        Matrix.translateM(mModelMatrix, 0, toCenter.x, toCenter.y, toCenter.z);

        refreshVectors();
    }

    public Vec3[] getPoints() {
        Vec3[] res = new Vec3[points.length];
        for (int i = 0; i < points.length; i++)
            res[i] = points[i].mul(mModelMatrix);
        return res;
    }

    private void processTrail() {
        boolean trailOn = (((MainActivity)core.context).trailOn);
        if(!wasTrailOn && trailOn) core.trail = new Trail(core);
        wasTrailOn = trailOn;
        if(trailOn) {
            if (Math.abs(rotation.y - prevRotY) > 1) {
                core.trail.addQuad();
                prevRotY = rotation.y;
            }
            core.trail.correctPoints();
        }
    }

    private void refreshVectors() {
        position = points[0].mul(mModelMatrix);
        frontWheels = (points[3].center(points[4])).mul(mModelMatrix);
        backWheels = (points[5].center(points[6])).mul(mModelMatrix);
    }

    private void moveForward(float step) {
        Matrix.translateM(mModelMatrix, 0, 0, 0, step);
        refreshVectors();

        wheelRots[0].x += Utils.getArc(step, FRONT_RAD);
        wheelRots[1].x += Utils.getArc(step, FRONT_RAD);
        wheelRots[2].x += Utils.getArc(step, BACK_RAD);
        wheelRots[3].x += Utils.getArc(step, BACK_RAD);
    }

    private void rotate(float theta) {
        Vec3 rotPoint = points[0];
        setRotation(rotation.y + theta, rotPoint);
        refreshVectors();

        wheelRots[0].x += Utils.getArc(Utils.getArcLength(theta, points[3].distance(points[0])), FRONT_RAD);
        wheelRots[1].x += Utils.getArc(Utils.getArcLength(theta, points[4].distance(points[0])), FRONT_RAD);
        wheelRots[2].x += Utils.getArc(Utils.getArcLength(theta, points[5].distance(points[0])), BACK_RAD);
        wheelRots[3].x += Utils.getArc(Utils.getArcLength(theta, points[6].distance(points[0])), BACK_RAD);
    }

    public void addTracetoryPoint(double x, double z) {
        x *= COORD_SCALE_X;
        z *= COORD_SCALE_Z;
        if(traectory.size() == 0) {
            COORD_OFFSET_X = x;
            COORD_OFFSET_Z = z;
            Matrix.setIdentityM(COORD_ROT_MAT, 0);
        }
        Vec3 v = new Vec3((float) (x - COORD_OFFSET_X), position.y, (float) (z - COORD_OFFSET_Z));
        traectory.add(v.mul(COORD_ROT_MAT));
    }

    public Float getDeviation() {
        if(core.line == null) return null;
        Vec3 pointA = core.line.pointA, pointB = core.line.pointB;
        if(pointA == null || pointB == null) return null;
        float dZ = pointB.z - pointA.z;
        float dX = pointB.x - pointA.x;

        float x1, x2;
        Vec3 lCorner = getPoints()[1], rCorner = getPoints()[2];

        Float result;
        if(dZ == 0) {
            result = Math.abs(pointA.z - position.z);
            x1 = Math.abs(pointA.z - lCorner.z);
            x2 = Math.abs(pointA.z - rCorner.z);
        } else if(dX == 0) {
            result = Math.abs(pointA.x - position.x);
            x1 = Math.abs(pointA.x - lCorner.x);
            x2 = Math.abs(pointA.x - rCorner.x);
        } else {
            float k = dZ / dX;
            float b = pointA.z - k*pointA.x;
            result = Math.abs(position.z - k*position.x - b) / (float)Math.sqrt(k*k + 1);
            x1 = Math.abs(lCorner.z - k*lCorner.x - b) / (float)Math.sqrt(k*k + 1);
            x2 = Math.abs(rCorner.z - k*rCorner.x - b) / (float)Math.sqrt(k*k + 1);
        }
        if(x1 > x2) result = result * -1;
        return result;
    }
}