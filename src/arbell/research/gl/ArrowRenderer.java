package arbell.research.gl;

import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ArrowRenderer extends Renderer
{
    int muColorHandle;

    FloatBuffer mVertexBuffer;
    FloatBuffer[] mColorBuffers;
    ByteBuffer[] mIndexBuffers;
    int[] iCount;

    public ArrowRenderer(String v, String f)
    {
        super(v, f);
    }

    protected void initVertex()
    {
        mColorBuffers = new FloatBuffer[6];
        mIndexBuffers = new ByteBuffer[6];
        iCount = new int[6];

        float[] vertices = new float[]{
                1, 0, 0.1f,
                0.4f, 0.6f, 0.1f,
                0.4f, -0.6f, 0.1f,
                0.4f, 0.2f, 0.1f,
                -1.0f, 0.2f, 0.1f,
                -1.0f, -0.2f, 0.1f,
                0.4f, -0.2f, 0.1f,
                1, 0, -0.1f,
                0.4f, 0.6f, -0.1f,
                0.4f, -0.6f, -0.1f,
                0.4f, 0.2f, -0.1f,
                -1.0f, 0.2f, -0.1f,
                -1.0f, -0.2f, -0.1f,
                0.4f, -0.2f, -0.1f,
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        byte[] up = new byte[]{
                0, 1, 2,
                3, 4, 5,
                5, 6, 3
        };

        byte[] down = new byte[]{
                7, 9, 8,
                10, 13, 12,
                12, 11, 10,
        };

        byte[] front = new byte[]{
                3, 10, 11,
                11, 4, 3,
        };

        byte[] back = new byte[]{
                6, 5, 12,
                12, 13, 6,
        };

        byte[] left = new byte[]{
                0, 7, 8,
                8, 1, 0,
                0, 2, 9,
                9, 7, 0,
        };

        byte[] right = new byte[]{
                4, 11, 12,
                12, 5, 4,
                1, 8, 10,
                10, 3, 1,
                6, 13, 9,
                9, 2, 6
        };

        byte[][] indices = new byte[][]{up, down, front, back, left, right};

        for (int i = 0; i < iCount.length; i++)
        {
            iCount[i] = indices[i].length;
        }

        for (int i = 0; i < mIndexBuffers.length; i++)
        {
            ByteBuffer bf = ByteBuffer.allocate(iCount[i]);
            bf.order(ByteOrder.nativeOrder());
            bf.put(indices[i]);
            bf.position(0);
            mIndexBuffers[i] = bf;
        }

        float[][] colors = new float[][]{
                {1, 1, 1, 0},
                {0.5f, 0, 0.5f, 0},
                {1f, 0.5f, 0.5f, 0},
                {0.5f, 0.5f, 1f, 0},
                {0.5f, 1f, 0.5f, 0},
                {1f, 1f, 0, 0}
        };

        for (int i = 0; i < mColorBuffers.length; i++)
        {
            bb = ByteBuffer.allocateDirect(4 * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
            fb.put(colors[i]);
            fb.position(0);
            mColorBuffers[i] = fb;
        }
    }

    @Override
    protected void getHandles()
    {
        super.getHandles();
        muColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        super.onDrawFrame(gl);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        for (int i = 0; i < mIndexBuffers.length; i++)
        {
            GLES20.glUniform4fv(muColorHandle, 1, mColorBuffers[i]);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, iCount[i], GLES20.GL_UNSIGNED_BYTE, mIndexBuffers[i]);
        }
    }
}
