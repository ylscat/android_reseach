package arbell.research.gl.new_arch;

import android.content.res.Resources;
import android.opengl.Matrix;

import java.util.ArrayList;

/**
 * Author: YinLanShan
 * Date: 14-2-19
 * Time: 13:18
 */
public class Scene
{
    public ArrayList<PhysicsObject> mObjects = new ArrayList<PhysicsObject>();

    public float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    public float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    public float[] mDirection = {0, 0, -1};
    public float[] mPosition = {0, 1.72f, 4f};

    private Resources mRes;
    public ProjectionControl mPrjControl = new ProjectionControl();

    public Scene(Resources res)
    {
        mRes = res;
        Matrix.setLookAtM(mVMatrix, 0,
                mPosition[0], mPosition[1],  mPosition[2],
                mPosition[0] + mDirection[0],
                mPosition[1] + mDirection[1],
                mPosition[2] + mDirection[2],
                0, 1, 0);
    }

    public void init()
    {
        mObjects.add(new Ball(mRes));
    }

    public void move(float d)
    {
        float dx = mDirection[0]*d;
        float dy = mDirection[1]*d;
        mPosition[0] += dx;
        mPosition[1] += dy;
        Matrix.translateM(mVMatrix, 0, -dx, -dy, 0);
    }

    public void rotate(float az, float ay)
    {
        az = az > 0.1f ? 0.1f : (az < -0.1f ? -0.1f : az);
        float cos = (float)Math.sqrt(1 - az*az);
        float s = mDirection[1]*cos + mDirection[0]*az;
        float c = (float)Math.sqrt(1 - s*s);
        if(s > 0 && az > mDirection[0] || s <= 0 && az < -mDirection[0])
        {
            c = -c;
        }

        mDirection[0] = c;
        mDirection[1] = s;
        ay = ay > 0.1f ? 0.1f : (ay < -0.1f ? -0.1f : ay);
        float t = mDirection[2];
        if(t*ay < 1)
        {
            t = (t + ay)/(1 - t*ay);
            if(t > 0)
                mDirection[2] = 0;
            else
                mDirection[2] = t;
        }

        Matrix.setLookAtM(mVMatrix, 0,
                mPosition[0], mPosition[1],  mPosition[2],
                mPosition[0] + mDirection[0],
                mPosition[1] + mDirection[1],
                mPosition[2] + mDirection[2],
                0, 0, 1);
    }
}
