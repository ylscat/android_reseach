precision mediump float;

varying vec3 vPosition;
uniform mat4 uRMatrix;
uniform vec4 uColor;

const vec4 bg = vec4(0.7, 0.7, 0.7, 0.0);
void main()                         
{
    vec4 position = uRMatrix * vec4(vPosition, 1.0);
    float w = (1.0 - position.z)/2.0;
    //w = pow(1.0 - w, 2.0);
    w = 1.0 - w;
    gl_FragColor = bg*(1.0 - w) + uColor*w;
}