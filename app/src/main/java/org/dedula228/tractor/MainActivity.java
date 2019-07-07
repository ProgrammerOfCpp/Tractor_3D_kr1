package org.dedula228.tractor;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.dedula228.tractor.objects.RouteLine;
import org.dedula228.tractor.util.Assets;
import java.util.*;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    public Button left, right, addPoint_A_B, zoomPlus, zoomMinus, trail_on, trail_on1, trail_off, trail_pause, curve_mode;
    public TextView put;
    public boolean trailOn = false, vpr = false;
    public int a_b = 0;
    Core core;
    int times = 0;
    public List<String > fileStrings = new ArrayList<>();
    private Timer mTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Assets.load(this);
        readFile();
        setContentView(R.layout.main);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 24, 0);
        glSurfaceView.setRenderer(core = new Core(this));
        ((LinearLayout) findViewById(R.id.renderer)).addView(glSurfaceView);

        addPoint_A_B = (Button) findViewById(R.id.button337);

        trail_on = (Button) findViewById(R.id.button3);
        trail_off = (Button) findViewById(R.id.button33_1);
        trail_on1 = (Button) findViewById(R.id.button33);
        trail_pause = (Button) findViewById(R.id.button332);
        zoomPlus = (Button) findViewById(R.id.minusss);
        zoomMinus = (Button) findViewById(R.id.plusss);
        curve_mode = (Button) findViewById(R.id.curve_btn);

        addPoint_A_B.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a_b++;
                if (a_b == 1) {
                    core.line.pointA = new Vec3(core.tractor.position);
                    core.ALabel.position = new Vec3(core.tractor.position);
                    addPoint_A_B.setText(R.string.b);
                } else if (a_b == 2) {
                    core.line.pointB = new Vec3(core.tractor.position);
                    core.BLabel.position = new Vec3(core.tractor.position);
                    addPoint_A_B.setText(R.string.a_b);
                } else if (a_b == 3) {
                    addPoint_A_B.setText(R.string.a);
                    a_b = 0;
                    core.line.pointA = core.line.pointB = null;
                    core.ALabel.position = new Vec3(0, -10, 0);
                    core.BLabel.position = new Vec3(0, -10, 0);
                }
            }
        });
        zoomMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float len = core.tractor.camera2Tractor.len();
                if (len < 20)
                    core.tractor.camera2Tractor = core.tractor.camera2Tractor.setLength(len + 1);
            }
        });
        zoomPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float len = core.tractor.camera2Tractor.len();
                if (len > 6) {
                    core.tractor.camera2Tractor = core.tractor.camera2Tractor.setLength(len - 1);
                }
            }
        });
        trail_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailOn = true;
                vpr = true;
                trail_on.setVisibility(View.INVISIBLE);
                trail_on.setAlpha(0);
                trail_off.setVisibility(View.VISIBLE);
                trail_off.setAlpha(0.7f);
                trail_pause.setVisibility(View.VISIBLE);
                trail_pause.setAlpha(0.7f);


                if(mTimer == null) {
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(times < fileStrings.size()) {
                                /*String[] tokens = fileStrings.get(times).split(";");
                                final double TARGET_LATITUDE = Double.parseDouble(tokens[0]);
                                final double TARGET_LONGITUDE = Double.parseDouble(tokens[1]);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        core.tractor.addTracetoryPoint(TARGET_LATITUDE, TARGET_LONGITUDE);
                                    }
                                });
                                times++;*/
                            }
                        }
                    }, 1, 1);
                } else {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        });
        trail_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailOn = false;
                vpr = false;
                trail_off.setVisibility(View.INVISIBLE);
                trail_off.setAlpha(0);
                trail_on1.setVisibility(View.VISIBLE);
                trail_on1.setAlpha(0.7f);
            }
        });
        trail_on1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailOn = true;
                vpr = true;
                trail_on1.setVisibility(View.INVISIBLE);
                trail_on1.setAlpha(0);
                trail_off.setVisibility(View.VISIBLE);
                trail_off.setAlpha(0.7f);
            }
        });
        trail_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailOn = false;
                vpr = false;
                trail_off.setVisibility(View.INVISIBLE);
                trail_off.setAlpha(0);
                trail_on1.setVisibility(View.INVISIBLE);
                trail_on1.setAlpha(0);
                trail_on.setVisibility(View.VISIBLE);
                trail_on.setAlpha(0.7f);
                trail_pause.setVisibility(View.INVISIBLE);
                trail_pause.setAlpha(0);
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        });
        curve_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //core.line.curveMode = !core.line.curveMode;
            }
        });
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }


    public void readFile() {
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.file_davlek));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            fileStrings.add(line);
        }
        scanner.close();
    }
}
