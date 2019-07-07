uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;  
uniform vec3 u_Scale;   		
		  			
attribute vec4 a_Position;		
attribute vec3 a_Normal;  		
attribute vec2 a_UV;  	
   
varying vec3 v_Position;   
varying vec3 v_Normal;
varying vec2 v_UV;

void main()                                                 	
{
	vec4 position = a_Position * vec4(u_Scale, 1);
	vec3 normal = normalize(a_Normal * u_Scale);
	
	v_Position = vec3(u_MVMatrix * position);
	v_Normal = normalize(vec3(u_MVMatrix * vec4(normal, 0.0)));
	v_UV = a_UV;
	gl_Position = u_MVPMatrix * position;                       		  
}  