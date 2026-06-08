#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 黑畫面覆蓋程度
// 0.0 = 正常
// 1.0 = 完全黑
uniform float Blackout;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    float amount = clamp(Blackout, 0.0, 1.0);

    // 黑畫面淡出
    fragColor = vec4(mix(color.rgb, vec3(0.0), amount), color.a);
}
