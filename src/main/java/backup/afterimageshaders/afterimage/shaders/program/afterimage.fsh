#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 col = texture(DiffuseSampler, texCoord);
    float g = dot(col.rgb, vec3(0.299, 0.587, 0.114));
    fragColor = vec4(vec3(g), 1.0);
}
