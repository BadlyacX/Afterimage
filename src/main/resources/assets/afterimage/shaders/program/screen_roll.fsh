#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 畫面旋轉角度（弧度）
// 用來模擬頭部傾斜 / 畫面翻轉
uniform float ScreenRoll;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    // 將 UV 中心移到畫面中央
    vec2 centeredCoord = texCoord - vec2(0.5);

    // 計算旋轉矩陣
    float rollSin = sin(ScreenRoll);
    float rollCos = cos(ScreenRoll);

    // 套用 2D 旋轉後再移回原點
    vec2 rolledCoord = vec2(
        centeredCoord.x * rollCos - centeredCoord.y * rollSin,
        centeredCoord.x * rollSin + centeredCoord.y * rollCos
    ) + vec2(0.5);

    fragColor = texture(DiffuseSampler, rolledCoord);
}
