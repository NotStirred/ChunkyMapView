#version 330
#extension GL_ARB_explicit_uniform_location : require
#extension GL_ARB_separate_shader_objects : enable

in vec3 vp;
in vec2 inTexCoord;

uniform mat4 MVP;

out vec2 texCoord;

void main() {
  gl_Position =  MVP * vec4(vp,1);
  texCoord = inTexCoord;
}