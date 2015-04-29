package arbell.research.gl.new_arch;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Author: YinLanShan
 * Date: 14-2-19
 * Time: 11:37
 */
public class SimpleRenderer implements GLSurfaceView.Renderer
{
    private Scene mScene;

    public SimpleRenderer(Scene scene)
    {
        mScene = scene;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        mScene.init();
        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);
        mScene.mPrjControl.setup(width, height, mScene.mProjMatrix);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        for(PhysicsObject b : mScene.mObjects)
            b.draw(mScene);
    }
}
