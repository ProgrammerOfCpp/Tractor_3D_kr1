package org.dedula228.tractor;

import android.content.Context;
import org.dedula228.tractor.util.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLObject {

    public final Core core;
    public final Context context;

    protected int shader;


    public GLObject(Core core, int vertexShader, int fragmentShader) {
        this.core = core;
        this.context = core.context;
        this.shader = Utils.loadShader(context, vertexShader, fragmentShader);

        core.objects.add(this);
    }

    public void update(float delta) {}

    public void render() {}
}
