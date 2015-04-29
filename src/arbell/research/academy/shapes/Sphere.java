package arbell.research.academy.shapes;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import arbell.research.gl.Renderer;
import arbell.research.util.GestureControl;


/**
 * Author: YinLanShan
 * Date: 13-6-19
 * Time: 16:46
 */
public class Sphere extends Activity
{
    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGLView = new GLSurfaceView(this);
        setContentView(mGLView);

        Resources r = getResources();
        String v = Renderer.loadShaderFromAssetsFile(r, "sphere_vertex.c");
        String f = Renderer.loadShaderFromAssetsFile(r, "sphere_fragment.c");
        SphereRenderer renderer = new SphereRenderer(v, f);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

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
