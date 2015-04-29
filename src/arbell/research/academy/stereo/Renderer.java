package arbell.research.academy.stereo;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Author: YinLanShan
 * Date: 14-8-26
 * Time: 11:23
 */
public class Renderer implements GLSurfaceView.Renderer
{
    private Resources mRes;
    private TextView mTextView;

    private static Handler sHandler = new Handler();

    public int muMVPMatrixHandle;
    public int maPositionHandle;
    public int maColorHandle, maNormalHandle;
    public int muCameraHandle, muLightDirectionHandle, muRMatrixHandle;

    public float[] mProjMatrix = new float[16];
    public float[] mProjMatrixLeft = new float[16];
    public float[] mProjMatrixRight = new float[16];
    public float[] mVMatrix = new float[16];
    public float[] mVMatrixLeft = new float[16];
    public float[] mVMatrixRight = new float[16];
    public float[] mMVPMatrix = new float[16];
    public float[] mCameraPosition = {0, 0, 1.8f};
    public float[] mLightDirection = {0.6f, 0.8f, 0};
    public int mWidth, mHeight;

//    public ArrayList<Polyhedron> mObjects = new ArrayList<Polyhedron>();
    private Face20 mObj1, mObj2;
    private float mDegree;

    public Renderer(Resources mRes, TextView textView)
    {
        this.mRes = mRes;
        mTextView = textView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        String vertexShader = Renderer.loadShaderFromAssetsFile(mRes, "stereo/vertex.c");
        String fragmentShader = Renderer.loadShaderFromAssetsFile(mRes, "stereo/fragment.c");
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        int program = createProgram(vertex, fragment);
        bindHandles(program);

        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(program);
        mObj1 = new Face20();
        mObj2 = new Face20();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);

        float tangentOfAngle = 0.5f;
        float near = 0.01f;
        float side = near*tangentOfAngle;
        float offset = 0.0003f;
        if (width > height)
        {
            float ratio = (float) height / width;
            Matrix.frustumM(mProjMatrix, 0, -side, side, -ratio*side, ratio*side, near, 100);
            Matrix.frustumM(mProjMatrixLeft, 0, -side - offset, side - offset, -ratio*side, ratio*side, near, 100);
            Matrix.frustumM(mProjMatrixRight, 0, -side + offset, side + offset, -ratio*side, ratio*side, near, 100);
        } else
        {
            float ratio = (float) width / height;
            Matrix.frustumM(mProjMatrix, 0, -ratio*side, ratio*side, -side, side, near, 100);
            Matrix.frustumM(mProjMatrixLeft, 0, -ratio*side - offset, ratio*side - offset, -side, side, near, 100);
            Matrix.frustumM(mProjMatrixRight, 0, -ratio*side + offset, ratio*side + offset, -side, side, near, 100);
        }
        mWidth = width;
        mHeight = height;
        Matrix.setLookAtM(mVMatrixLeft, 0, mCameraPosition[0] + .027f,
                mCameraPosition[1], mCameraPosition[2],
                0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.setLookAtM(mVMatrixRight, 0, mCameraPosition[0] - .027f,
                mCameraPosition[1], mCameraPosition[2],
                0f, 0f, 0f, 0f, 1.0f, 0.0f);
//        sHandler.post(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                mTextView.setText(String.format("%d X %d", mWidth, mHeight));
//            }
//        });
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniform3fv(muCameraHandle,1, mCameraPosition, 0);
        GLES20.glUniform3fv(muLightDirectionHandle, 1, mLightDirection, 0);
        mDegree += 0.5f;
        if(mDegree == 360)
            mDegree = 0;
        mObj1.rotate(mDegree);
        mObj2.rotate(mDegree + 180);
        mObj1.draw(this);
        mObj2.draw(this);
    }

    private void bindHandles(int program)
    {
        muMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        maColorHandle = GLES20.glGetAttribLocation(program, "aColor");
        maNormalHandle = GLES20.glGetAttribLocation(program, "aNormal");
        muCameraHandle = GLES20.glGetUniformLocation(program, "uCamera");
        muLightDirectionHandle = GLES20.glGetUniformLocation(program, "uLightDirection");
        muRMatrixHandle = GLES20.glGetUniformLocation(program, "uRMatrix");
    }

    private int createProgram(int vertexShader, int fragmentShader)
    {
        int program = GLES20.glCreateProgram();
        if(program == 0)
            throw new RuntimeException("Could not create program");
        GLES20.glAttachShader(program, vertexShader);
        int error = GLES20.glGetError();
        if(error != GLES20.GL_NO_ERROR)
            throw new RuntimeException("Error in attaching vertex shader");
        GLES20.glAttachShader(program, fragmentShader);
        error = GLES20.glGetError();
        if(error != GLES20.GL_NO_ERROR)
            throw new RuntimeException("Error in attaching vertex shader");

        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE)
        {
            String log = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Could not link program.\n" + log);
        }
        return program;
    }

    private int loadShader(int type, String source)
    {
        int shader = GLES20.glCreateShader(type);
        if(shader == 0)
            throw new RuntimeException("Could not create shader, type:"+type);

        GLES20.glShaderSource(shader, source);

        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0)
        {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Could not compile shader.\n" + log);
        }
        return shader;
    }

    public static String loadShaderFromAssetsFile(Resources res, String file)
    {
        try
        {
            StringBuilder sb = new StringBuilder(200);
            InputStream in = res.getAssets().open(file);
            InputStreamReader reader = new InputStreamReader(in);

            char[] buf = new char[100];
            int c = reader.read(buf);
            while (c > 0)
            {
                sb.append(buf, 0, c);
                c = reader.read(buf);
            }

            reader.close();
            return sb.toString().replaceAll("\\r\\n", "\n");
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
