#version 400

in vec3 vp;
in vec2 aTexCoord;

out vec2 TexCoord;

uniform mat4 MVP;

void main(){
  gl_Position =  MVP * vec4(vp,1);
  TexCoord = aTexCoord;
}