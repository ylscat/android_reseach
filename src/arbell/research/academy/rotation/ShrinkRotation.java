package arbell.research.academy.rotation;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
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
public class ShrinkRotation extends Activity implements GestureControl.RotationListener
{
    private GLSurfaceView mGLView;
    private float[] mRotationMatrix;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shrink_rotation);
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
        gc.setRotationListener(this);

        mTextView = (TextView) findViewById(R.id.angle);
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

    @SuppressWarnings({"unused"})
    public void shrink(View button)
    {
        float[] Rm = mRotationMatrix;
        float cos = (Rm[0] + Rm[5] + Rm[10] - 1) / 2;

        float xsin = (Rm[6] - Rm[9]) / 2;
        float ysin = (Rm[8] - Rm[2]) / 2;
        float zsin = (Rm[1] - Rm[4]) / 2;
        Rm[6] -= xsin;
        Rm[9] += xsin;
        Rm[8] -= ysin;
        Rm[2] += ysin;
        Rm[1] -= zsin;
        Rm[4] += zsin;
        Rm[0] -= cos;
        Rm[5] -= cos;
        Rm[10] -= cos;
        final float BLEND_RATIO = 0.99f;
        float ncos = 1 - BLEND_RATIO + BLEND_RATIO * cos;
        float nsin = (float) Math.sqrt(1 - ncos * ncos);
        float sin = (float) Math.sqrt(1 - cos * cos);
        float ratio = sin == 0 ? 1 : nsin / sin;
        xsin = xsin * ratio;
        ysin = ysin * ratio;
        zsin = zsin * ratio;
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                int index = i + j * 4;
                Rm[index] *= BLEND_RATIO;
                if (i == j)
                    Rm[index] += ncos;
            }
        }
        Rm[6] += xsin;
        Rm[9] -= xsin;
        Rm[8] += ysin;
        Rm[2] -= ysin;
        Rm[1] += zsin;
        Rm[4] -= zsin;

        double angle = Math.acos(ncos) * 180 / Math.PI;
        mTextView.setText(String.format("Angle: %.1f\n" +
                "%.2f %.2f %.2f\n" +
                "%.2f %.2f %.2f\n" +
                "%.2f %.2f %.2f\n", angle,
                Rm[0], Rm[4], Rm[8],
                Rm[1], Rm[5], Rm[9],
                Rm[2], Rm[6], Rm[10]));
        mGLView.requestRender();
    }

    @Override
    public void onRotation(float[] Rm)
    {
        float cos = (Rm[0] + Rm[5] + Rm[10] - 1) / 2;
        double angle = Math.acos(cos) * 180 / Math.PI;
        mTextView.setText(String.format("Angle: %.1f", angle));
    }
}
