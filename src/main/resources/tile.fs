#version 400

in vec2 TexCoord;

out vec4 frag_colour;

uniform vec4 in_colour;
uniform sampler2D tex;

void main() {
    frag_colour = texture(tex, TexCoord) * in_colour;
}