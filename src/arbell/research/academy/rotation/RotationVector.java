package arbell.research.academy.rotation;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import arbell.research.R;
import arbell.research.gl.ArrowRenderer;
import arbell.research.gl.Renderer;
import arbell.research.util.GestureControl;

/**
 * Author: YinLanShan
 * Date: 13-11-21
 * Time: 14:32
 */
public class RotationVector extends Activity implements SeekBar.OnSeekBarChangeListener,
        GestureControl.RotationComputer
{
    private GLSurfaceView mGLView;
    private float[] mRotationMatrix;
    private float[] mAxis = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotation_vector);
        mGLView = (GLSurfaceView) findViewById(R.id.gl_view);
        Resources r = getResources();
        String v = Renderer.loadShaderFromAssetsFile(r, "arrow_vertex.c");
        String f = Renderer.loadShaderFromAssetsFile(r, "arrow_fragment.c");
        ArrowRenderer renderer = new ArrowRenderer(v, f);
        mRotationMatrix = renderer.getRotationMatrix();
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        GestureControl gc = new GestureControl(mGLView, mRotationMatrix);
        mGLView.setOnTouchListener(gc);
        gc.setRotationComputer(this);

        SeekBar seekBar = (SeekBar) findViewById(R.id.angle_seekbar);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        if (!fromUser)
            return;
        TextView angle = (TextView) findViewById(R.id.angle);
        angle.setText(String.valueOf(progress));
        double rad = progress * Math.PI / 180;
        float cos = (float) Math.cos(rad);
        float icos = 1 - cos;
        float sin = (float) Math.sqrt(1 - cos * cos);
        float x = mAxis[0], y = mAxis[1], z = mAxis[2];
        float[] R = mRotationMatrix;
        R[0] = cos + icos * x * x;
        R[1] = icos * x * y + sin * z;
        R[2] = icos * x * z - y * sin;
        R[4] = icos * x * y - z * sin;
        R[5] = cos + icos * y * y;
        R[6] = icos * y * z + x * sin;
        R[8] = icos * x * z + y * sin;
        R[9] = icos * y * z - x * sin;
        R[10] = cos + icos * z * z;
        mGLView.requestRender();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public boolean computeRotation(float ax, float ay, float az, float[] R)
    {
        double len = Math.sqrt((ax * ax + ay * ay + az * az));
        if (len < 0.002f)
            return false;
        float k1 = (float) (Math.sin(len) / len);
        float k2 = (float) ((1 - Math.cos(len)) / len / len);
        float[] B = new float[16], B2 = new float[16], dR = new float[16];
        dR[15] = 1;

        /**      0  -az   ay
         *  B = az   0   -ax
         *     -ay   ax   0
         */
        B[1] = az;
        B[2] = -ay;
        B[4] = -az;
        B[6] = ax;
        B[8] = ay;
        B[9] = -ax;

        Matrix.multiplyMM(B2, 0, B, 0, B, 0);

        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                int index = i * 4 + j;
                dR[index] = k1 * B[index] + k2 * B2[index];
                if (i == j)
                    dR[index] += 1;
            }
        }

        System.arraycopy(R, 0, B, 0, R.length);
        Matrix.multiplyMM(R, 0, dR, 0, B, 0);
        updateAxis(R);
        return true;
    }

    public void updateAxis(float[] matrix)
    {
        float trace = matrix[0] + matrix[5] + matrix[10];
        float cos = (trace - 1) / 2;
        float angle = (float) Math.acos(cos);
        TextView tv = (TextView) findViewById(R.id.angle);
        int degree = (int) (angle * 180 / 3.14f);
        tv.setText(String.valueOf(degree));
        SeekBar sb = (SeekBar) findViewById(R.id.angle_seekbar);
        sb.setProgress(degree);
        float sin = (float) Math.sqrt(1 - cos * cos);
        tv = (TextView) findViewById(R.id.axis);
        if (sin == 0)
        {
            tv.setText("(0, 0, 0)");
            return;
        }
        float[] axis = mAxis;
        axis[0] = (matrix[6] - matrix[9]) / sin / 2;
        axis[1] = (matrix[8] - matrix[2]) / sin / 2;
        axis[2] = (matrix[1] - matrix[4]) / sin / 2;
        tv.setText(String.format("(%.2f %.2f %.2f)", axis[0], axis[1], axis[2]));
    }
}
