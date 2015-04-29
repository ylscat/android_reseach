package arbell.research.academy.shapes;

import android.opengl.GLES20;
import arbell.research.gl.Renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Author: YinLanShan
 * Date: 13-7-5
 * Time: 17:18
 */
public class Face4Renderer extends Renderer
{
    FloatBuffer mVertexBuffer;
    FloatBuffer mColorBuffer;
    FloatBuffer mLineBuffer;
    FloatBuffer mLineColor;
    int maColorHandle;

    public Face4Renderer(String vertexShader, String fragmentShader)
    {
        super(vertexShader, fragmentShader);
    }

    @Override
    protected void initVertex()
    {
        super.initVertex();
        final float Rp = (float) Math.sqrt(8.0 / 9);
        final float H = -1 / 3f;
        final double Rad120Degree = Math.PI * 2 / 3;

        float[] vertices = new float[]{
                0, 0, 1,
                Rp, 0, H,
                (float) (Rp * Math.cos(Rad120Degree)),
                (float) (Rp * Math.sin(Rad120Degree)), H,
                (float) (Rp * Math.cos(-Rad120Degree)),
                (float) (Rp * Math.sin(-Rad120Degree)), H,
        };

        int[] faces = new int[]{
                0, 1, 2,
                0, 2, 3,
                0, 3, 1,
//                3, 2, 1
        };

        float[] colorsForEachFace = new float[]{
                1, 0, 0, 0.5f,
                0, 1, 0, 0.5f,
                0, 0, 1, 0.5f,
                0, 0, 0, 0.5f,
        };

        int bufSize = faces.length * 3 * 4;
        ByteBuffer bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuf = bb.asFloatBuffer();
        bufSize = faces.length * 4 * 4;
        bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer colorBuf = bb.asFloatBuffer();
        for (int i = 0; i < faces.length; i++)
        {
            int index = faces[i] * 3;
            for (int j = 0; j < 3; j++)
            {
                vertexBuf.put(vertices[index + j]);
            }
            index = i / 3 * 4;
            colorBuf.put(colorsForEachFace, index, 4);
        }
        vertexBuf.position(0);
        colorBuf.position(0);
        mVertexBuffer = vertexBuf;
        mColorBuffer = colorBuf;

        ArrayList<Integer> lines = getLines(faces);
        bufSize = lines.size() * 3 * 4;
        bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer lineVertex = bb.asFloatBuffer();
        bufSize = lines.size() * 4 * 4;
        bb = ByteBuffer.allocateDirect(bufSize);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer lineColor = bb.asFloatBuffer();
        final float[] BLACK = {0, 0, 0, 1};
        for (Integer i : lines)
        {
            for (int j = 0; j < 3; j++)
            {
                lineVertex.put(vertices[i * 3 + j]);
            }
            lineColor.put(BLACK);
        }
        lineVertex.position(0);
        lineColor.position(0);
        mLineBuffer = lineVertex;
        mLineColor = lineColor;
    }

    @Override
    protected void getHandles()
    {
        super.getHandles();
        maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        super.onSurfaceCreated(gl, config);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        super.onDrawFrame(gl);

        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false,
                4 * 4, mColorBuffer);
        GLES20.glEnableVertexAttribArray(maColorHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVertexBuffer.capacity() / 3);

        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                3 * 4, mLineBuffer);
        GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false,
                4 * 4, mLineColor);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, mLineBuffer.capacity() / 3);
    }
}
