#version 330 core

uniform sampler2D uTexture;
uniform float uOpacity;

in vec2 texCoord;
out vec4 fragmentColor;

void main()
{
    fragmentColor = texture(uTexture, texCoord);
    fragmentColor.a *= uOpacity;
}
