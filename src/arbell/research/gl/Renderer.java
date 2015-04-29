package arbell.research.gl;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Author: YinLanShan
 * Date: 13-5-17
 * Time: 18:51
 */
public class Renderer implements GLSurfaceView.Renderer
{
    protected float[] mRotationMatrix = new float[16];//4x4旋转矩阵
    protected float[] mProjMatrix = new float[16];//4x4矩阵 投影用
    protected float[] mVMatrix = new float[16];//摄像机位置朝向9参数矩阵
    protected float[] mMVPMatrix = new float[16];

    protected int mProgram;
    protected int muMVPMatrixHandle;
    protected int maPositionHandle;

    protected String mVertexShader, mFragmentShader;

    public Renderer()
    {
        Matrix.setIdentityM(mRotationMatrix, 0);
        mVertexShader =
                "uniform mat4 uMVPMatrix;\n" +
                        "attribute vec3 aPosition;\n" +
                        "varying vec3 vPosition;" +
                        "\n" +
                        "void main()\n" +
                        "{\n" +
                        "   gl_Position = uMVPMatrix * vec4(aPosition,1);\n" +
                        "   vPosition = aPosition;\n" +
                        "}";
        mFragmentShader =
                "precision mediump float;\n" +

                "varying vec3 vPosition;\n" +
                "void main()\n" +
                "{\n" +
                "   vec4 color;\n" +
                "   int i = int((vPosition.x + 1.0)/0.25);\n" +
                "   int j = int((vPosition.y + 1.0)/0.25);\n" +
                "   int k = int((vPosition.z + 1.0)/0.25);\n" +
                "   int whichColor = int(mod(float(i + j + k), 2.0));\n" +
                "   if(whichColor == 1) {\n" +
                "       color = vec4(1, 1, 1, 1);\n" +
                "   }\n" +
                "   else {\n" +
                "       color = vec4(0, 0, 0, 1);\n" +
                "   }\n" +
                "   gl_FragColor = color;\n" +
                "}";
    }

    public Renderer(String vertexShader, String fragmentShader)
    {
        Matrix.setIdentityM(mRotationMatrix, 0);
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public float[] getRotationMatrix()
    {
        return mRotationMatrix;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        String vertex = mVertexShader;
        String fragment = mFragmentShader;
        // Load shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        if (vertexShader == 0)
            throw new RuntimeException("Vertex shader load failed");
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        if (fragmentShader == 0)
            throw new RuntimeException("Fragment shader load failed");
        //Attach shaders to program
        int program = GLES20.glCreateProgram();
        if (program == 0)
            throw new RuntimeException("Can't create GLES program");
        GLES20.glAttachShader(program, vertexShader);
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
            throw new RuntimeException("Error on glAttachShader for vertex. Error:" + error);
        GLES20.glAttachShader(program, fragmentShader);
        error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR)
            throw new RuntimeException("Error on glAttachShader for fragment. Error:" + error);
        // Link program
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE)
        {
            Log.e("ES20_ERROR", "Could not link program: ");
            Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Link program failed");
        }
        mProgram = program;

        getHandles();
        initVertex();

        GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        GLES20.glUseProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);

        if (width > height)
        {
            float ratio = (float) height / width;
            Matrix.frustumM(mProjMatrix, 0, -1, 1, -ratio, ratio, 2, 10);
        } else
        {
            float ratio = (float) width / height;
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 2, 10);
        }

        Matrix.setLookAtM(mVMatrix, 0, 0f, 0f, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

//        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    protected void initVertex()
    {

    }

    protected void getHandles()
    {
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
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

    protected int loadShader(int shaderType, String source)
    {
        //创建一个新shader
        int shader = GLES20.glCreateShader(shaderType);
        //若创建成功则加载shader
        if (shader != 0)
        {
            //加载shader的源代码
            GLES20.glShaderSource(shader, source);
            //编译shader
            GLES20.glCompileShader(shader);
            //存放编译成功shader数量的数组
            int[] compiled = new int[1];
            //获取Shader的编译情况
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0)
            {//若编译失败则显示错误日志并删除此shader
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static ArrayList<Integer> getLines(ArrayList<Integer> faces)
    {
        ArrayList<Integer> result = new ArrayList<Integer>(faces.size());
        HashMap<Integer, HashSet<Integer>> lineMap = new HashMap<Integer, HashSet<Integer>>();
        int faceSize = faces.size() / 3;
        int[] index = new int[3];
        for (int i = 0; i < faceSize; i++)
        {
            index[0] = faces.get(i * 3);
            index[1] = faces.get(i * 3 + 1);
            index[2] = faces.get(i * 3 + 2);
            for (int j = 0; j < 3; j++)
            {
                int k = j == 2 ? 0 : j + 1;
                int index1 = index[j], index2 = index[k];
                if (index1 > index2)
                {
                    int temp = index1;
                    index1 = index2;
                    index2 = temp;
                }
                HashSet<Integer> set = lineMap.get(index1);
                if (set != null && set.contains(index2))
                    continue;
                if (set == null)
                {
                    set = new HashSet<Integer>(6);
                    lineMap.put(index1, set);
                }
                set.add(index2);
                result.add(index1);
                result.add(index2);
            }
        }
        return result;
    }

    public static ArrayList<Integer> getLines(int[] faces)
    {
        ArrayList<Integer> result = new ArrayList<Integer>(faces.length);
        HashMap<Integer, HashSet<Integer>> lineMap = new HashMap<Integer, HashSet<Integer>>();
        int faceSize = faces.length / 3;
        int[] index = new int[3];
        for (int i = 0; i < faceSize; i++)
        {
            index[0] = faces[i * 3];
            index[1] = faces[i * 3 + 1];
            index[2] = faces[i * 3 + 2];
            for (int j = 0; j < 3; j++)
            {
                int k = j == 2 ? 0 : j + 1;
                int index1 = index[j], index2 = index[k];
                if (index1 > index2)
                {
                    int temp = index1;
                    index1 = index2;
                    index2 = temp;
                }
                HashSet<Integer> set = lineMap.get(index1);
                if (set != null && set.contains(index2))
                    continue;
                if (set == null)
                {
                    set = new HashSet<Integer>(6);
                    lineMap.put(index1, set);
                }
                set.add(index2);
                result.add(index1);
                result.add(index2);
            }
        }
        return result;
    }
}
