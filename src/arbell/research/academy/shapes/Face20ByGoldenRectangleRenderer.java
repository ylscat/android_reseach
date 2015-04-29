package arbell.research.academy.shapes;

import android.opengl.GLES20;
import arbell.research.gl.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: YinLanShan
 * Date: 13-6-17
 * Time: 18:54
 */
public class Face20ByGoldenRectangleRenderer extends Renderer
{
    FloatBuffer mVertexBuffer;
    FloatBuffer mLineColor;
    FloatBuffer[] mColorBuffers;
    ByteBuffer[] mIndexBuffers;
    ByteBuffer mLineIndices;
    int muColorHandle;

    public Face20ByGoldenRectangleRenderer(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initVertex()
    {
        super.initVertex();
        final float b = 0.618f;
        float[] vertices = new float[]{
                b, 0, 1,
                -b, 0, 1,
                b, 0, -1,
                -b, 0, -1,
                1, b, 0,
                -1, b, 0,
                1, -b, 0,
                -1, -b, 0,
                0, 1, b,
                0, -1, b,
                0, 1, -b,
                0, -1, -b,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);


        byte[][] indices = new byte[][]{
                {0, 1, 2, 3},
                {4, 5, 6, 7},
                {8, 9, 10, 11}
        };

        float[][] colors = new float[][]{
                {1.0f, 0, 0, 0.5f},
                {0, 1.0f, 0, 0.5f},
                {0, 0, 1.0f, 0.5f}
        };

        mColorBuffers = new FloatBuffer[3];
        mIndexBuffers = new ByteBuffer[3];
        for (int i = 0; i < 3; i++)
        {
            ByteBuffer bf = ByteBuffer.allocate(indices[i].length);
            bf.order(ByteOrder.nativeOrder());
            bf.put(indices[i]);
            bf.position(0);
            mIndexBuffers[i] = bf;

            FloatBuffer fb = FloatBuffer.allocate(4);
            fb.put(colors[i]);
            fb.position(0);
            mColorBuffers[i] = fb;
        }

        byte[] lineIndices = new byte[]{
                0, 1, 0, 9, 0, 6, 0, 4, 0, 8,
                1, 8, 1, 5, 1, 7, 1, 9, /*9*/
                2, 3, 2, 11, 2, 6, 2, 4, 2, 10,
                3, 11, 3, 7, 3, 5, 3, 10,  /*18*/
                4, 6, 4, 8, 4, 10,
                5, 7, 5, 8, 5, 10,  /*24*/
                6, 9, 6, 11,
                7, 9, 7, 11,
                8, 10,
                9, 11  /*30*/
        };
        mLineIndices = ByteBuffer.allocate(lineIndices.length);
        mLineIndices.order(ByteOrder.nativeOrder());
        mLineIndices.put(lineIndices);
        mLineIndices.position(0);

        FloatBuffer fb = FloatBuffer.allocate(4);
        fb.put(new float[]{0, 0, 0, 1});
        fb.position(0);
        mLineColor = fb;
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
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndexBuffers[i].capacity(),
                    GLES20.GL_UNSIGNED_BYTE, mIndexBuffers[i]);
        }
        GLES20.glLineWidth(1f);
        GLES20.glUniform4fv(muColorHandle, 1, mLineColor);
        GLES20.glDrawElements(GLES20.GL_LINES, mLineIndices.capacity(),
                GLES20.GL_UNSIGNED_BYTE, mLineIndices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        super.onSurfaceCreated(gl, config);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }
}
