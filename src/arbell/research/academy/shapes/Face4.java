package arbell.research.academy.shapes;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import arbell.research.gl.Renderer;
import arbell.research.util.GestureControl;

/**
 * Author: YinLanShan
 * Date: 13-5-15
 * Time: 13:24
 */
public class Face4 extends Activity
{
    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        Resources r = getResources();
        String v = Renderer.loadShaderFromAssetsFile(r, "simple_vertex.c");
        String f = Renderer.loadShaderFromAssetsFile(r, "simple_fragment.c");
        Face4Renderer renderer = new Face4Renderer(v, f);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mGLView);

        GestureControl gc = new GestureControl(mGLView, renderer.getRotationMatrix());
        mGLView.setOnTouchListener(gc);
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
}
