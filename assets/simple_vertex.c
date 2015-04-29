uniform mat4 uMVPMatrix;
attribute vec4 aColor;
attribute vec3 aPosition;
varying  vec4 vColor;
varying vec3 vPosition;
void main()
{
   gl_Position = uMVPMatrix * vec4(aPosition,1);
   vPosition = aPosition;
   vColor = aColor;
}