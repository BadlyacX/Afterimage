#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 灰階混合強度
// 0.0 = 原色
// 1.0 = 完全灰階
uniform float GrayAmount;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // 使用標準亮度權重轉換灰階
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayscale = vec3(luminance);

    // 原畫面與灰階混合
    fragColor = vec4(mix(color.rgb, grayscale, clamp(GrayAmount, 0.0, 1.0)), color.a);
}
