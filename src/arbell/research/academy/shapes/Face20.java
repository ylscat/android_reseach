package arbell.research.academy.shapes;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import arbell.research.R;
import arbell.research.gl.Renderer;
import arbell.research.util.GestureControl;

/**
 * Author: YinLanShan
 * Date: 13-5-15
 * Time: 13:24
 */
public class Face20 extends Activity implements RadioGroup.OnCheckedChangeListener
{
    private GLSurfaceView mGLView1, mGLView2, mGLView3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Resources r = getResources();
        String v = Renderer.loadShaderFromAssetsFile(r, "arrow_vertex.c");
        String f = Renderer.loadShaderFromAssetsFile(r, "arrow_fragment.c");
        Renderer renderer0 = new Face20ByGoldenRectangleRenderer(v, f);
        v = Renderer.loadShaderFromAssetsFile(r, "face20_sym_vertex.c");
        f = Renderer.loadShaderFromAssetsFile(r, "face20_sym_fragment.c");
        Renderer renderer1 = new Face20BySymmetryRenderer(v, f);
        v = Renderer.loadShaderFromAssetsFile(r, "face20_lighted_vertex.c");
        f = Renderer.loadShaderFromAssetsFile(r, "face20_lighted_fragment.c");
        Renderer renderer2 = new Face20Lighted(v, f);

        setContentView(R.layout.face20);
        GLSurfaceView glView = (GLSurfaceView) findViewById(R.id.gl_view1);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(renderer0);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        GestureControl gc = new GestureControl(glView, renderer0.getRotationMatrix());
        glView.setOnTouchListener(gc);
        mGLView1 = glView;
        glView = (GLSurfaceView) findViewById(R.id.gl_view2);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(renderer1);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        gc = new GestureControl(glView, renderer1.getRotationMatrix());
        glView.setOnTouchListener(gc);
        mGLView2 = glView;
        glView = (GLSurfaceView) findViewById(R.id.gl_view3);
        glView.setEGLContextClientVersion(2);
        glView.setRenderer(renderer2);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        gc = new GestureControl(glView, renderer2.getRotationMatrix());
        glView.setOnTouchListener(gc);
        mGLView3 = glView;

        RadioGroup rg = (RadioGroup) findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(this);
        int checkedId = rg.getCheckedRadioButtonId();
        onCheckedChanged(rg, checkedId);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLView1.onResume();
        mGLView2.onResume();
        mGLView3.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLView1.onPause();
        mGLView2.onPause();
        mGLView3.onPause();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch (checkedId)
        {
            case R.id.radio_button0:
                mGLView1.setVisibility(View.VISIBLE);
                mGLView2.setVisibility(View.INVISIBLE);
                mGLView3.setVisibility(View.INVISIBLE);
                break;
            case R.id.radio_button1:
                mGLView1.setVisibility(View.INVISIBLE);
                mGLView2.setVisibility(View.VISIBLE);
                mGLView3.setVisibility(View.INVISIBLE);
                break;
            case R.id.radio_button2:
                mGLView1.setVisibility(View.INVISIBLE);
                mGLView2.setVisibility(View.INVISIBLE);
                mGLView3.setVisibility(View.VISIBLE);
                break;
        }
    }
}
