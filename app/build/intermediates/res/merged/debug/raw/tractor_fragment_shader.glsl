precision mediump float;

uniform float useTexture;
uniform sampler2D u_Texture;

uniform float d;
uniform vec3 u_LightPos;
uniform vec4 u_Ambient;
uniform vec4 u_Diffuse;
uniform vec4 u_Specular;

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_UV;

void main (void)
{
	// calculate color
	vec4 color;
	if(useTexture == 1.0)
		color = texture2D(u_Texture, v_UV);
	else
		color = vec4(1.0, 1.0, 1.0, 1.0);
	// calculate lightning
	vec3 lightVector = normalize(u_LightPos - v_Position);
    float diffuse;
	if (gl_FrontFacing) {
        diffuse = max(dot(v_Normal, lightVector), 0.0);
    } else {
    	diffuse = max(dot(-v_Normal, lightVector), 0.0);
    }
	diffuse = diffuse + 0.9;
	// calculate result
	vec4 resultColor = color * (u_Ambient + (u_Diffuse * diffuse));
	resultColor.w = d;
    gl_FragColor = resultColor;
}