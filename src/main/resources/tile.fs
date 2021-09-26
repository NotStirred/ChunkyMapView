#version 330
#extension GL_ARB_explicit_uniform_location : require
#extension GL_ARB_separate_shader_objects : enable

in vec2 texCoord;

uniform vec4 in_color;
uniform sampler2D tex;

out vec4 frag_color;

void main() {
    frag_color = texture(tex, texCoord) * in_color;
}