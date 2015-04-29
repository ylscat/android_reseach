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

/**
 * Author: YinLanShan
 * Date: 13-11-20
 * Time: 15:26
 */
public class RotationMatrix extends Activity implements SeekBar.OnSeekBarChangeListener
{
    private GLSurfaceView mGLView;
    private float[] mRotationMatrix;
    private float[] mEulerAngle = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotation_matrix);
        mGLView = (GLSurfaceView) findViewById(R.id.gl_view);
        Resources r = getResources();
        String v = Renderer.loadShaderFromAssetsFile(r, "arrow_vertex.c");
        String f = Renderer.loadShaderFromAssetsFile(r, "arrow_fragment.c");
        ArrowRenderer renderer = new ArrowRenderer(v, f);
        mRotationMatrix = renderer.getRotationMatrix();
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        SeekBar seekBar = (SeekBar) findViewById(R.id.angle_x);
        seekBar.setOnSeekBarChangeListener(this);
        onProgressChanged(seekBar, seekBar.getProgress(), false);
        seekBar = (SeekBar) findViewById(R.id.angle_y);
        seekBar.setOnSeekBarChangeListener(this);
        onProgressChanged(seekBar, seekBar.getProgress(), false);
        seekBar = (SeekBar) findViewById(R.id.angle_z);
        seekBar.setOnSeekBarChangeListener(this);
        onProgressChanged(seekBar, seekBar.getProgress(), false);
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

    protected void rotate(float ax, float ay, float az)
    {
        double len = Math.sqrt((ax * ax + ay * ay + az * az));
        if (len == 0)
        {
            Matrix.setIdentityM(mRotationMatrix, 0);
            return;
        }
        float k1 = (float) (Math.sin(len) / len);
        float k2 = (float) ((1 - Math.cos(len)) / len / len);
        float[] B = new float[16], B2 = new float[16], dR = mRotationMatrix;
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
        mGLView.requestRender();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
        switch (seekBar.getId())
        {
            case R.id.angle_x:
                TextView label = (TextView) findViewById(R.id.label_x);
                int angle = seekBar.getProgress() - 180;
                label.setText("Angle X: " + angle);
                mEulerAngle[0] = (float) (angle * Math.PI / 180);
                rotate(mEulerAngle[0], mEulerAngle[1], mEulerAngle[2]);
                break;
            case R.id.angle_y:
                label = (TextView) findViewById(R.id.label_y);
                angle = seekBar.getProgress() - 180;
                label.setText("Angle Y: " + angle);
                mEulerAngle[1] = (float) (angle * Math.PI / 180);
                rotate(mEulerAngle[0], mEulerAngle[1], mEulerAngle[2]);
                break;
            case R.id.angle_z:
                label = (TextView) findViewById(R.id.label_z);
                angle = seekBar.getProgress() - 180;
                label.setText("Angle Z: " + angle);
                mEulerAngle[2] = (float) (angle * Math.PI / 180);
                rotate(mEulerAngle[0], mEulerAngle[1], mEulerAngle[2]);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }
}
