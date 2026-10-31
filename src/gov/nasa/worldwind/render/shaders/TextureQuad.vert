#version 330 core

layout(location = 0) in vec2 vertexPosition;
layout(location = 1) in vec2 textureCoordinate;

uniform vec2 uViewport;
uniform vec4 uRectangle;

out vec2 texCoord;

void main()
{
    vec2 screenPosition = uRectangle.xy + vertexPosition * uRectangle.zw;
    vec2 clipPosition = (screenPosition / uViewport) * 2.0 - 1.0;
    gl_Position = vec4(clipPosition, 0.0, 1.0);
    texCoord = textureCoordinate;
}
