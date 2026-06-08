#version 150

uniform sampler2D DiffuseSampler;

// 凹凸效果強度
// 0.0 = 直接 passthrough（不套效果）
// 1.0 = 完整凹凸效果
uniform float BumpyAmount;

// 用來計算 oneTexel，替代 bumpy.vsh 的 varying
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 c = texture(DiffuseSampler, texCoord);

    float amount = clamp(BumpyAmount, 0.0, 1.0);
    if (amount < 0.001) {
        fragColor = vec4(c.rgb, 1.0);
        return;
    }

    vec2 oneTexel = 1.0 / InSize;
    vec4 u = texture(DiffuseSampler, texCoord + vec2(0.0, -oneTexel.y));
    vec4 d = texture(DiffuseSampler, texCoord + vec2(0.0,  oneTexel.y));
    vec4 l = texture(DiffuseSampler, texCoord + vec2(-oneTexel.x, 0.0));
    vec4 r = texture(DiffuseSampler, texCoord + vec2( oneTexel.x, 0.0));

    vec4 nc = normalize(c);
    vec4 nu = normalize(u);
    vec4 nd = normalize(d);
    vec4 nl = normalize(l);
    vec4 nr = normalize(r);

    float du = dot(nc, nu);
    float dd = dot(nc, nd);
    float dl = dot(nc, nl);
    float dr = dot(nc, nr);

    float i = 64.0;

    float f = 1.0;
    f += (du * i) - (dd * i);
    f += (dr * i) - (dl * i);

    vec4 bumpyColor = c * clamp(f, 0.5, 2.0);

    fragColor = vec4(mix(c.rgb, bumpyColor.rgb, amount), 1.0);
}
