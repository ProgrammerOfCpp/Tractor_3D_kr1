uniform mat4 u_MVPMatrix;
uniform mat4 u_MVMatrix;     		
		  			
attribute vec4 a_Position;		
attribute vec3 a_Normal;  		
attribute vec2 a_UV;  	
		  
varying vec3 v_Position;   
varying vec3 v_Normal;	    		
varying vec2 v_UV;
		  
// The entry point for our vertex shader.  
void main()                                                 	
{                                                         
	// Transform the vertex into eye space. 	
	v_Position = vec3(u_MVMatrix * a_Position);            
		
	// Pass through the color.
	v_UV = a_UV;
	
	// Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
          
	// gl_Position is a special variable used to store the final position.
	// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
	gl_Position = u_MVPMatrix * a_Position;                       		  
}      