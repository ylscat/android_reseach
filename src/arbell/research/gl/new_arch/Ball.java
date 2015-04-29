package arbell.research.gl.new_arch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import arbell.research.R;
import arbell.research.gl.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * Author: YinLanShan
 * Date: 14-2-21
 * Time: 14:56
 */
public class Ball extends PhysicsObject
{
    public static final float BALL_RADIUS = 0.5f/2;
    private IntBuffer mIndexBuffer;

    public Ball(Resources res)
    {
        String vertexShader = Renderer.loadShaderFromAssetsFile(res, "vertex.sh");
        String fragShader = Renderer.loadShaderFromAssetsFile(res, "frag.sh");
        Bitmap texture = BitmapFactory.decodeResource(res, R.raw.tennis_ball);
        int[] textureParams = new int[]{GLES20.GL_NEAREST, GLES20.GL_NEAREST,
                GLES20.GL_MIRRORED_REPEAT, GLES20.GL_MIRRORED_REPEAT};
        init(vertexShader, fragShader, texture, textureParams);
        texture.recycle();
        Matrix.translateM(mTransform, 0, 0, 2, 0);
    }

    @Override
    void initAttributes()
    {
        final float R = BALL_RADIUS;
        final float DELTA_ANGLE = (float)Math.PI/10;
        final float TEXTURE_HEIGHT_WIDTH_RATIO = 256/64f;
        ArrayList<Float> vertexList = new ArrayList<Float>(190*3);
        ArrayList<Float> textureList = new ArrayList<Float>(190*2);
        //Add top dot
        vertexList.add(0f);
        vertexList.add(R);
        vertexList.add(0f);
        textureList.add(0f);
        textureList.add(0f);
        //Add other dots
        for(int i = 1; i < 10; i++)
        {
            float attitude = i*DELTA_ANGLE;
            float h = (float)(Math.cos(attitude)*R);
            float r = (float)(Math.sin(attitude)*R);
            float deltaS = TEXTURE_HEIGHT_WIDTH_RATIO*r/R/20;
            float t = (R - h)/2/R;
            for(int j = 0; j < 20; j++)
            {
                float azimuth = j*DELTA_ANGLE;
                float z = (float)(Math.cos(azimuth)*r);
                float x = (float)(Math.sin(azimuth)*r);
                vertexList.add(x);
                vertexList.add(h);
                vertexList.add(z);

                float s = deltaS*j;
                textureList.add(s);
                textureList.add(t);
            }
        }

        //Add bottom dot
        vertexList.add(0f);
        vertexList.add(-R);
        vertexList.add(0f);
        textureList.add(0f);
        textureList.add(1f);

        ArrayList<Integer> indexList = new ArrayList<Integer>(20 + 20 + 7*40);
        int index0, index1 = 0;
        for(int i = 0; i < 10; i++)
        {
            index0 = index1;
            index1 = i == 0 ? 1 : index1 + 20;
            for(int j = 0; j < 20; j++)
            {
                int point0 = index0 + j;
                int point1 = index1 + j;
                int point2 = j == 19 ? index1 : point1 + 1;
                int point3 = j == 19 ? index0 : point0 + 1;
                if(i < 9)
                {
                    if(i == 0)
                        point0 = index0;
                    indexList.add(point0);
                    indexList.add(point1);
                    indexList.add(point2);
                }
                if(i > 0)
                {
                    if(i == 9)
                        point2 = index1;
                    indexList.add(point2);
                    indexList.add(point3);
                    indexList.add(point0);
                }
            }
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexList.size()*4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        for(Float f : vertexList)
            fb.put(f);
        fb.position(0);
        mVertexBuffer = fb;

        bb = ByteBuffer.allocateDirect(textureList.size()*4);
        bb.order(ByteOrder.nativeOrder());
        fb = bb.asFloatBuffer();
        for(Float f : textureList)
            fb.put(f);
        fb.position(0);
        mTexCoordBuffer = fb;

        bb = ByteBuffer.allocateDirect(indexList.size()*4);
        bb.order(ByteOrder.nativeOrder());
        IntBuffer ib = bb.asIntBuffer();
        for(Integer i : indexList)
            ib.put(i);

        ib.position(0);
        mIndexBuffer = ib;
    }

    @Override
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

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexBuffer.capacity(),
                GLES20.GL_UNSIGNED_INT, mIndexBuffer);
    }
}
