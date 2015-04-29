uniform mat4 uMVPMatrix;
uniform mat4 uRMatrix;
uniform vec3 uLightDirection;
uniform vec3 uCamera;
attribute vec3 aPosition;
attribute vec3 aNormal;
attribute vec4 aColor;
varying vec4 vColor;

const float SHININESS = 50.0;
const float AMBIENT_WEIGHT = 0.35;
const float DIFFUSE_WEIGHT = 0.8;
const float SPECULAR_WEIGHT = 0.7;

void computeLight(inout vec3 color, in vec4 position)
{
    vec3 normal = normalize((uRMatrix*vec4(aNormal, 0.0)).xyz);
    vec3 eye = normalize(uCamera - position.xyz);
    vec3 halfVector = normalize(uLightDirection + eye);

    float diffuse = max(0.0, dot(uLightDirection, normal));
    float dotHalfVector = dot(normal, halfVector);
    float specular = 0.0;
    if(dotHalfVector > 0.8) {
        specular = pow(dotHalfVector, SHININESS);
    }

    vec3 temp = aColor.rgb;
    color = temp*AMBIENT_WEIGHT
            + temp*DIFFUSE_WEIGHT*diffuse
            + temp*SPECULAR_WEIGHT*specular
            + (1.0, 1.0, 1.0)*SPECULAR_WEIGHT*specular;
}

void main()
{
    vec3 color;
    vec4 position = uRMatrix*vec4(aPosition, 1.0);
    computeLight(color, position);
    vColor = vec4(color, 1.0);
    gl_Position = uMVPMatrix * position;
}