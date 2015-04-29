package arbell.research.gl.new_arch;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: YinLanShan
 * Date: 14-2-18
 * Time: 15:57
 */
public class PhysicsObject
{
    int mProgram;
    int uMVPMatrixHandle;
    int aPositionHandle;
    int aTexCoordHandle;
    int uTexHandle;

    int mTexID;
    FloatBuffer mVertexBuffer;
    FloatBuffer mTexCoordBuffer;

    public float[] mTransform = new float[16];
    public float[] mMVPMatrix = new float[16];

    public void init(String vertexShader, String fragmentShader, Bitmap texture, int[] textureParams)
    {
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mProgram = createProgram(vertex, fragment);
        mTexID = createTexture(texture, textureParams);
        Matrix.setIdentityM(mTransform, 0);
        bindHandles();
        initAttributes();
    }

    void bindHandles()
    {
        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        aPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        aTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        uTexHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
    }

    void initAttributes()
    {
        float[] vertices = {
                0, 0, 0,
                1, 0, 0,
                1, 1, 0,
                1, 1, 0,
                0, 1, 0,
                0, 0, 0
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(vertices);
        fb.position(0);
        mVertexBuffer = fb;
        float[] texCoord = {
                0, 0,
                1, 0,
                1, 1,
                1, 1,
                0, 1,
                0, 0
        };
        bb = ByteBuffer.allocateDirect(texCoord.length*4);
        bb.order(ByteOrder.nativeOrder());
        fb = bb.asFloatBuffer();
        fb.put(vertices);
        fb.position(0);
        mTexCoordBuffer = fb;
    }

    public void draw(Scene scene)
    {
        GLES20.glUseProgram(mProgram);

        Matrix.multiplyMM(mMVPMatrix, 0, scene.mVMatrix, 0, mTransform, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, scene.mProjMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexID);
        GLES20.glUniform1i(uTexHandle, 0);

        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT,
                false, 3*4, mVertexBuffer);
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT,
                false, 2*4, mTexCoordBuffer);
        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexBuffer.capacity()/3);
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

    public int createTexture(Bitmap bitmap, int[] params)
    {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId=textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, params[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, params[1]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, params[2]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, params[3]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textureId;
    }
}
