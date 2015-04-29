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
public class Face20BySymmetryRenderer extends Renderer
{
    FloatBuffer mVertexBuffer;
    FloatBuffer mLineColor;
    FloatBuffer[] mColorBuffers;
    ByteBuffer[] mIndexBuffers;
    ByteBuffer mLineIndices;
    int muColorHandle;
    int muBiasHandle;

    public Face20BySymmetryRenderer(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initVertex()
    {
        super.initVertex();
        final float SQRT5 = (float) Math.sqrt(5);
        final float R = (float) Math.sqrt((5 + SQRT5) / 8);
        final float Rp = (float) Math.sqrt((5 + SQRT5) / 10);
        final float H = (float) Math.sqrt((5 + SQRT5) / 40);
        final double rad72Degree = Math.PI * 2 / 5;

        float[] vertices = new float[]{
                0, 0, R,
                (float) (Rp * Math.cos(rad72Degree * 0)),
                (float) (Rp * Math.sin(rad72Degree * 0)), H,
                (float) (Rp * Math.cos(rad72Degree * 1)),
                (float) (Rp * Math.sin(rad72Degree * 1)), H,
                (float) (Rp * Math.cos(rad72Degree * 2)),
                (float) (Rp * Math.sin(rad72Degree * 2)), H,
                (float) (Rp * Math.cos(rad72Degree * 3)),
                (float) (Rp * Math.sin(rad72Degree * 3)), H,
                (float) (Rp * Math.cos(rad72Degree * 4)),
                (float) (Rp * Math.sin(rad72Degree * 4)), H,
                (float) (Rp * Math.cos(rad72Degree * 0.5)),
                (float) (Rp * Math.sin(rad72Degree * 0.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 1.5)),
                (float) (Rp * Math.sin(rad72Degree * 1.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 2.5)),
                (float) (Rp * Math.sin(rad72Degree * 2.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 3.5)),
                (float) (Rp * Math.sin(rad72Degree * 3.5)), -H,
                (float) (Rp * Math.cos(rad72Degree * 4.5)),
                (float) (Rp * Math.sin(rad72Degree * 4.5)), -H,
                0, 0, -R
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);


        byte[][] indices = new byte[][]{
                {0, 1, 2, 3, 4, 5, 1},
                {1, 6, 2, 7, 3, 8, 4, 9, 5, 10, 1, 6},
                {11, 10, 9, 8, 7, 6, 10}
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
                0, 1, 0, 2, 0, 3, 0, 4, 0, 5,
                1, 2, 2, 3, 3, 4, 4, 5, 5, 1,/*10*/
                1, 6, 6, 2, 2, 7, 7, 3, 3, 8,
                8, 4, 4, 9, 9, 5, 5, 10, 10, 1, /*20*/
                6, 7, 7, 8, 8, 9, 9, 10, 10, 6,
                11, 6, 11, 7, 11, 8, 11, 9, 11, 10 /*20*/
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
        muBiasHandle = GLES20.glGetUniformLocation(mProgram, "uBias");
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        super.onDrawFrame(gl);
        GLES20.glLineWidth(2f);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        GLES20.glUniform1f(muBiasHandle, 0.0f);
        GLES20.glUniform4fv(muColorHandle, 1, mLineColor);
        GLES20.glDrawElements(GLES20.GL_LINES, mLineIndices.capacity(),
                GLES20.GL_UNSIGNED_BYTE, mLineIndices);

        GLES20.glUniform1f(muBiasHandle, 0f);
        GLES20.glUniform4fv(muColorHandle, 1, mColorBuffers[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mIndexBuffers[0].capacity(),
                GLES20.GL_UNSIGNED_BYTE, mIndexBuffers[0]);
        GLES20.glUniform4fv(muColorHandle, 1, mColorBuffers[1]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndexBuffers[1].capacity(),
                GLES20.GL_UNSIGNED_BYTE, mIndexBuffers[1]);
        GLES20.glUniform4fv(muColorHandle, 1, mColorBuffers[2]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, mIndexBuffers[2].capacity(),
                GLES20.GL_UNSIGNED_BYTE, mIndexBuffers[2]);

//        GLES20.glLineWidth(2f);

//        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);
//        GLES20.glPolygonOffset(-0.1f, 0);

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
