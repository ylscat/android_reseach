package arbell.research.academy.stereo;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import arbell.research.R;

/**
 * Author: YinLanShan
 * Date: 14-8-26
 * Time: 10:53
 */
public class HalfDisplay extends Activity
{
    private GLSurfaceView mGLView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.half_display);
        mGLView = (GLSurfaceView)findViewById(R.id.gl_view);
        TextView tv = (TextView)findViewById(R.id.text);
        Resources res = getResources();
        Renderer renderer = new Renderer(res, tv);
        mGLView.setEGLContextClientVersion(2);
        mGLView.setRenderer(renderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
