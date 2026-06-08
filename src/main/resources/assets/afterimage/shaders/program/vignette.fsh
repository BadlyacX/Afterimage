#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 暗角強度
// 0.0 = 無暗角
// 1.0 = 完整暗角
uniform float VignetteAmount;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // 計算距離畫面中心的距離
    vec2 centered = texCoord - vec2(0.5);
    float dist = length(centered);

    // 產生平滑暗角遮罩，越靠近邊緣越暗
    float vignette = smoothstep(0.30, 0.80, dist) * 0.9;
    float amount = clamp(VignetteAmount, 0.0, 1.0);

    fragColor = vec4(color.rgb * (1.0 - vignette * amount), color.a);
}
