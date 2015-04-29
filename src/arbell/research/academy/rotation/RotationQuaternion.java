package arbell.research.academy.rotation;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
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
public class RotationQuaternion extends Activity implements SeekBar.OnSeekBarChangeListener
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
        float len = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (len < 0.002)
            return;
        float cos = (float) Math.cos(len / 2);
        float sin = (float) Math.sqrt(1 - cos * cos);
        float s = sin / len;
        float[] dq = new float[4];
        dq[0] = s * ax;
        dq[1] = s * ay;
        dq[2] = s * az;
        dq[3] = cos;
        float[] q = {0, 0, 0, 1};

        //Multiply [dq X q]

        float x = dq[3] * q[0] + dq[0] * q[3] + dq[1] * q[2] - dq[2] * q[1];
        float y = dq[3] * q[1] + dq[1] * q[3] + dq[0] * q[2] - dq[2] * q[0];
        float z = dq[3] * q[2] + dq[2] * q[3] + dq[0] * q[1] - dq[1] * q[0];
        float w = dq[3] * q[3] - dq[0] * q[0] - dq[1] * q[1] - dq[2] * q[2];


        float sx = x * x;
        float sy = y * y;
        float sz = z * z;
        float[] result_matrix = mRotationMatrix;
        result_matrix[0] = 1 - 2 * (sy + sz);
        result_matrix[5] = 1 - 2 * (sx + sz);
        result_matrix[10] = 1 - 2 * (sx + sy);
        float xy = x * y;
        float zw = z * w;
        result_matrix[1] = 2 * (xy + zw);
        result_matrix[4] = 2 * (xy - zw);
        float xz = x * z;
        float yw = y * w;
        result_matrix[2] = 2 * (xz - yw);
        result_matrix[8] = 2 * (xz + yw);
        float yz = y * z;
        float xw = x * w;
        result_matrix[6] = 2 * (yz + xw);
        result_matrix[9] = 2 * (yz - xw);
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
