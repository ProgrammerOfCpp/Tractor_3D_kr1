package org.dedula228.tractor.util;

import android.content.Context;
import android.widget.Toast;

public class Assets {
    public static OBJModel tractor, backWheel, frontWheel, wing, vprick;

    public static void load(Context context) {
        Toast.makeText(context, "Loading...", Toast.LENGTH_LONG).show();
        frontWheel = new OBJModel(context,"p_k.obj");
        tractor = backWheel = new OBJModel(context, "z_k.obj");
        tractor = new OBJModel(context, "tr_bes_krila.obj");
        wing = new OBJModel(context, "krilo.obj");
    }
}