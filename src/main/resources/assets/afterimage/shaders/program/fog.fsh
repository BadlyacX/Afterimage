#version 150

uniform sampler2D DiffuseSampler;

// 霧效強度
// 0.0 = 無霧
// 1.0 = 完整迷霧
uniform float FogAmount;

// 時間參數，用於霧的動態漂移
uniform float Time;

in vec2 texCoord;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i),              hash(i + vec2(1.0, 0.0)), u.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    float amount = clamp(FogAmount, 0.0, 1.0);
    if (amount < 0.001) {
        fragColor = vec4(color.rgb, 1.0);
        return;
    }

    // 雙層漂移噪點模擬霧氣流動
    vec2 scroll = texCoord + vec2(Time * 0.012, Time * 0.008);
    float fog1 = smoothNoise(scroll * 3.5);
    float fog2 = smoothNoise(scroll * 7.0 + vec2(0.4, 0.2));
    float fogDensity = fog1 * 0.7 + fog2 * 0.3;

    // 淡灰色霧氣，對應 pale_plains 生態系 fog_color（0xCCCCCC）
    vec3 fogColor = vec3(0.8, 0.8, 0.8);

    float blend = fogDensity * 0.55 * amount;
    fragColor = vec4(mix(color.rgb, fogColor, blend), 1.0);
}
