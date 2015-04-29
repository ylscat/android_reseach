package arbell.research.academy.shapes;

import android.opengl.GLES20;
import arbell.research.gl.Renderer;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Author: YinLanShan
 * Date: 13-7-6
 * Time: 22:31
 */
public class Face12Renderer extends Renderer
{
    FloatBuffer mVertexBuffer;
    ByteBuffer mIndices;
    int muColorHandle;

    public Face12Renderer(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initVertex()
    {
        super.initVertex();
        final double SQRT5 = Math.sqrt(5);
        final double DEGREE_72 = Math.PI * 2 / 5;
        double E = (SQRT5 - 1) / Math.sqrt(3);
        double h0 = Math.sqrt(5 + 2 * SQRT5) / (5 - SQRT5) * E;
        double r0 = Math.sqrt(2 / (5 - SQRT5)) * E;
        double h1 = h0 - r0;
        double r1 = r0 * (SQRT5 + 1) / 2;

        float[] vertices = new float[]{
                (float) (r0 * Math.cos(DEGREE_72 * 0)),
                (float) (r0 * Math.sin(DEGREE_72 * 0)),
                (float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 1)),
                (float) (r0 * Math.sin(DEGREE_72 * 1)),
                (float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 2)),
                (float) (r0 * Math.sin(DEGREE_72 * 2)),
                (float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 3)),
                (float) (r0 * Math.sin(DEGREE_72 * 3)),
                (float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 4)),
                (float) (r0 * Math.sin(DEGREE_72 * 4)),
                (float) h0,
                (float) (r1 * Math.cos(DEGREE_72 * 0)),
                (float) (r1 * Math.sin(DEGREE_72 * 0)),
                (float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 1)),
                (float) (r1 * Math.sin(DEGREE_72 * 1)),
                (float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 2)),
                (float) (r1 * Math.sin(DEGREE_72 * 2)),
                (float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 3)),
                (float) (r1 * Math.sin(DEGREE_72 * 3)),
                (float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 4)),
                (float) (r1 * Math.sin(DEGREE_72 * 4)),
                (float) h1,
                //------------------------lower vertices
                (float) (r1 * Math.cos(DEGREE_72 * 0.5)),
                (float) (r1 * Math.sin(DEGREE_72 * 0.5)),
                -(float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 1.5)),
                (float) (r1 * Math.sin(DEGREE_72 * 1.5)),
                -(float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 2.5)),
                (float) (r1 * Math.sin(DEGREE_72 * 2.5)),
                -(float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 3.5)),
                (float) (r1 * Math.sin(DEGREE_72 * 3.5)),
                -(float) h1,
                (float) (r1 * Math.cos(DEGREE_72 * 4.5)),
                (float) (r1 * Math.sin(DEGREE_72 * 4.5)),
                -(float) h1,
                (float) (r0 * Math.cos(DEGREE_72 * 0.5)),
                (float) (r0 * Math.sin(DEGREE_72 * 0.5)),
                -(float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 1.5)),
                (float) (r0 * Math.sin(DEGREE_72 * 1.5)),
                -(float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 2.5)),
                (float) (r0 * Math.sin(DEGREE_72 * 2.5)),
                -(float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 3.5)),
                (float) (r0 * Math.sin(DEGREE_72 * 3.5)),
                -(float) h0,
                (float) (r0 * Math.cos(DEGREE_72 * 4.5)),
                (float) (r0 * Math.sin(DEGREE_72 * 4.5)),
                -(float) h0,
        };

        byte[] indices = new byte[]{
                0, 1, 1, 2, 2, 3, 3, 4, 4, 0,
                0, 5, 1, 6, 2, 7, 3, 8, 4, 9,
                5, 10, 10, 6, 6, 11, 11, 7, 7, 12,
                12, 8, 8, 13, 13, 9, 9, 14, 14, 5,
                10, 15, 11, 16, 12, 17, 13, 18, 14, 19,
                15, 16, 16, 17, 17, 18, 18, 19, 19, 15
        };

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        mIndices = ByteBuffer.allocate(indices.length);
        mIndices.order(ByteOrder.nativeOrder());
        mIndices.put(indices);
        mIndices.position(0);
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
        GLES20.glUniform4f(muColorHandle, 0, 0, 0, 1);
        GLES20.glDrawElements(GLES20.GL_LINES, mIndices.capacity(),
                GLES20.GL_UNSIGNED_BYTE, mIndices);
    }
}
