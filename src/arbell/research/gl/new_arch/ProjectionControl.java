package arbell.research.gl.new_arch;

import android.graphics.RectF;
import android.opengl.Matrix;

/**
 * Author: YinLanShan
 * Date: 14-2-20
 * Time: 15:12
 */
public class ProjectionControl
{
    public RectF mProjection = new RectF();
    public float mNear = 1;
    public float mFar = 100;
    public float mSize = 0.5f;

    public void setup(int width, int height, float[] projectMatrix)
    {
        float ratio;
        if (width > height)
        {
            ratio = (float) height / width;
            mProjection.set(-1, ratio, 1, -ratio);
        }
        else
        {
            ratio = (float) width / height;
            mProjection.set(-ratio, 1, ratio, -1);
        }

        update(projectMatrix);
    }

    public void update(float[] projectMatrix)
    {
        Matrix.frustumM(projectMatrix, 0,
                mSize*mProjection.left,
                mSize*mProjection.right,
                mSize*mProjection.bottom,
                mSize*mProjection.top, mNear, mFar);
    }
}
