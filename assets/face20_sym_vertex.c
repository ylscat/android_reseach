uniform mat4 uMVPMatrix; //总变换矩阵
uniform vec4 uColor;
uniform float uBias;

attribute vec3 aPosition;  //顶点位置

varying  vec4 vColor;  //用于传递给片元着色器的变量
varying vec3 vPosition;

void main()
{
    vec3 p = aPosition;
    if(uBias > 0.0) {
        vec3 b = normalize(aPosition);
        p = p + b*uBias;
    }
    gl_Position = uMVPMatrix * vec4(p, 1); //根据总变换矩阵计算此次绘制此顶点位置
    vColor = uColor;
}