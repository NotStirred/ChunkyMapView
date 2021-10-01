#version 330
#extension GL_ARB_explicit_uniform_location : require
#extension GL_ARB_separate_shader_objects : enable

in vec2 texCoord;

uniform vec4 in_color;
uniform sampler2D tex;

out vec4 frag_color;

void main() {
    vec4 texColor = texture(tex, texCoord);
    if(texColor.a < 0.001) //discard fragment if the alpha is low
        discard;

    frag_color = texColor * in_color;
}