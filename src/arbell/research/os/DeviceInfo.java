package arbell.research.os;

import android.app.Activity;
import android.graphics.Typeface;
import android.opengl.GLES10;
import android.os.Bundle;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created on 2015/8/20.
 */
public class DeviceInfo extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setText("Max GL texture size: " + getMaxTextureSize());
        setContentView(tv);
    }

    public int getMaxTextureSize()
    {
        // approach adopted from: http://stackoverflow.com/questions/26985858/gles10-glgetintegerv-returns-0-in-lollipop-only
        EGL10 egl = (EGL10) EGLContext.getEGL();

        EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] vers = new int[2];
        egl.eglInitialize(dpy, vers);

        int[] configAttr = {
                EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                EGL10.EGL_LEVEL, 0,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,
                EGL10.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];

        int[] numConfig = new int[1];

        egl.eglChooseConfig(dpy, configAttr, configs, 1, numConfig);

        if (numConfig[0] == 0) {
            return -1;
        }

        EGLConfig config = configs[0];

        int[] surfAttr = {
                EGL10.EGL_WIDTH, 64,
                EGL10.EGL_HEIGHT, 64,
                EGL10.EGL_NONE
        };

        EGLSurface surf = egl.eglCreatePbufferSurface(dpy, config, surfAttr);

        final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;  // missing in EGL10

        int[] ctxAttrib = {
                EGL_CONTEXT_CLIENT_VERSION, 1,
                EGL10.EGL_NONE
        };

        EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, ctxAttrib);

        egl.eglMakeCurrent(dpy, surf, surf, ctx);

        int[] maxSize = new int[1];

        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);

        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        egl.eglDestroySurface(dpy, surf);
        egl.eglDestroyContext(dpy, ctx);
        egl.eglTerminate(dpy);

        return maxSize[0];
    }
}
