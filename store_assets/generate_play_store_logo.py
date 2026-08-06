import math
import struct
import zlib

WIDTH = 512
HEIGHT = 512

def create_png(width, height, pixels):
    raw_data = bytearray()
    for y in range(height):
        raw_data.append(0) # filter type 0
        offset = y * width * 4
        raw_data.extend(pixels[offset:offset + width * 4])
    
    compressed = zlib.compress(raw_data, 9)
    
    png = bytearray(b'\x89PNG\r\n\x1a\n')
    
    # IHDR
    ihdr_data = struct.pack('!IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data)
    png.extend(struct.pack('!I', len(ihdr_data)))
    png.extend(b'IHDR')
    png.extend(ihdr_data)
    png.extend(struct.pack('!I', ihdr_crc))
    
    # IDAT
    idat_crc = zlib.crc32(b'IDAT' + compressed)
    png.extend(struct.pack('!I', len(compressed)))
    png.extend(b'IDAT')
    png.extend(compressed)
    png.extend(struct.pack('!I', idat_crc))
    
    # IEND
    iend_crc = zlib.crc32(b'IEND')
    png.extend(struct.pack('!I', 0))
    png.extend(b'IEND')
    png.extend(struct.pack('!I', iend_crc))
    
    return bytes(png)

# Helper color interpolation functions
def lerp(a, b, t):
    return a + (b - a) * max(0.0, min(1.0, t))

def blend_color(bg, fg):
    a_fg = fg[3] / 255.0
    if a_fg <= 0:
        return bg
    if a_fg >= 1:
        return fg
    a_bg = bg[3] / 255.0
    a_out = a_fg + a_bg * (1 - a_fg)
    if a_out <= 0:
        return (0, 0, 0, 0)
    r = int((fg[0] * a_fg + bg[0] * a_bg * (1 - a_fg)) / a_out)
    g = int((fg[1] * a_fg + bg[1] * a_bg * (1 - a_fg)) / a_out)
    b = int((fg[2] * a_fg + bg[2] * a_bg * (1 - a_fg)) / a_out)
    a = int(a_out * 255)
    return (r, g, b, a)

def get_shield_dist(px, py, cx=256, top_y=95, width=256, side_drop=38, bottom_y=394):
    # Evaluates signed distance / normalized inside ratio for shield path
    dx = abs(px - cx)
    half_w = width / 2.0
    
    if py < top_y:
        return 1.0 # outside top
    if py <= top_y + side_drop:
        # Linear top edge scaling
        t = (py - top_y) / side_drop
        cur_w = half_w
        if dx > cur_w:
            return 1.0
        return 0.0
    elif py <= bottom_y:
        # Curved shield bottom taper
        t = (py - (top_y + side_drop)) / (bottom_y - (top_y + side_drop))
        # Taper shape
        cur_w = half_w * (1.0 - (t ** 1.6))
        if dx > cur_w:
            return 1.0
        return 0.0
    return 1.0

pixels = bytearray(WIDTH * HEIGHT * 4)

def set_pixel(x, y, color):
    if 0 <= x < WIDTH and 0 <= y < HEIGHT:
        offset = (y * WIDTH + x) * 4
        cur = (pixels[offset], pixels[offset+1], pixels[offset+2], pixels[offset+3])
        out = blend_color(cur, color)
        pixels[offset] = out[0]
        pixels[offset+1] = out[1]
        pixels[offset+2] = out[2]
        pixels[offset+3] = out[3]

# Render canvas
for y in range(HEIGHT):
    for x in range(WIDTH):
        # 1. Background linear gradient (#060913 to #0F172A to #1E1B4B)
        t_bg = (x + y) / (WIDTH + HEIGHT)
        if t_bg < 0.5:
            tb = t_bg * 2
            r = int(lerp(0x06, 0x0F, tb))
            g = int(lerp(0x09, 0x17, tb))
            b = int(lerp(0x13, 0x2A, tb))
        else:
            tb = (t_bg - 0.5) * 2
            r = int(lerp(0x0F, 0x1E, tb))
            g = int(lerp(0x17, 0x1B, tb))
            b = int(lerp(0x2A, 0x4B, tb))
        
        # Ambient Radial Glow
        dist_c = math.hypot(x - 256, y - 256)
        if dist_c < 220:
            glow_t = (1 - dist_c / 220) ** 1.8
            r = min(255, int(r + 0x3B * glow_t * 0.45))
            g = min(255, int(g + 0x82 * glow_t * 0.45))
            b = min(255, int(b + 0xF6 * glow_t * 0.45))
        
        offset = (y * WIDTH + x) * 4
        pixels[offset] = r
        pixels[offset+1] = g
        pixels[offset+2] = b
        pixels[offset+3] = 255

# Supersampling rasterizer for high quality smooth antialiasing
def render_smooth():
    SAMPLES = 2
    for y in range(HEIGHT):
        for x in range(WIDTH):
            # Check components
            color_acc = [0, 0, 0, 0]
            samples_cnt = 0
            
            for sy in range(SAMPLES):
                for sx in range(SAMPLES):
                    px = x + (sx + 0.5) / SAMPLES
                    py = y + (sy + 0.5) / SAMPLES
                    
                    # 1. Outer Aura Shield
                    in_aura = get_shield_dist(px, py, top_y=76, width=294, side_drop=47, bottom_y=417) == 0.0
                    # 2. Main Shield
                    in_main = get_shield_dist(px, py, top_y=95, width=256, side_drop=38, bottom_y=394) == 0.0
                    # 3. Inner Bezel
                    in_bezel = get_shield_dist(px, py, top_y=114, width=218, side_drop=33, bottom_y=365) == 0.0
                    
                    # Padlock Shackle
                    # Outer Arc: R_out=52, Center=(256,175), Inner Arc: R_in=24
                    dx_s = px - 256
                    dy_s = py - 175
                    d_shackle = math.hypot(dx_s, dy_s)
                    
                    in_shackle = False
                    if py <= 175:
                        if 24 <= d_shackle <= 52:
                            in_shackle = True
                    elif 175 < py <= 218:
                        if (204 <= px <= 232) or (280 <= px <= 308):
                            in_shackle = True
                    
                    # Padlock Body White Base (x: 190..322, y: 213..341, rx=19)
                    in_body = False
                    bx0, bx1 = 190, 322
                    by0, by1 = 213, 341
                    br = 19
                    if bx0 <= px <= bx1 and by0 <= py <= by1:
                        # Corner checks
                        if px < bx0 + br and py < by0 + br:
                            in_body = math.hypot(px - (bx0 + br), py - (by0 + br)) <= br
                        elif px > bx1 - br and py < by0 + br:
                            in_body = math.hypot(px - (bx1 - br), py - (by0 + br)) <= br
                        elif px < bx0 + br and py > by1 - br:
                            in_body = math.hypot(px - (bx0 + br), py - (by1 - br)) <= br
                        elif px > bx1 - br and py > by1 - br:
                            in_body = math.hypot(px - (bx1 - br), py - (by1 - br)) <= br
                        else:
                            in_body = True
                            
                    # Inner Cyan Core (x: 200..312, y: 223..331, rx=10)
                    in_cyan = False
                    cx0, cx1 = 200, 312
                    cy0, cy1 = 223, 331
                    cr = 10
                    if cx0 <= px <= cx1 and cy0 <= py <= cy1:
                        if px < cx0 + cr and py < cy0 + cr:
                            in_cyan = math.hypot(px - (cx0 + cr), py - (cy0 + cr)) <= cr
                        elif px > cx1 - cr and py < cy0 + cr:
                            in_cyan = math.hypot(px - (cx1 - cr), py - (cy0 + cr)) <= cr
                        elif px < cx0 + cr and py > cy1 - cr:
                            in_cyan = math.hypot(px - (cx0 + cr), py - (cy1 - cr)) <= cr
                        elif px > cx1 - cr and py > cy1 - cr:
                            in_cyan = math.hypot(px - (cx1 - cr), py - (cy1 - cr)) <= cr
                        else:
                            in_cyan = True

                    # Keyhole Slot (Midnight Cutout)
                    # Circle at (256, 265, r=19) + trapezoid to (256, 306)
                    in_keyhole = False
                    dk = math.hypot(px - 256, py - 265)
                    if dk <= 19:
                        in_keyhole = True
                    elif 265 < py <= 306:
                        # trapezoid widening slightly
                        kw = 7 + (py - 265) * 0.15
                        if abs(px - 256) <= kw:
                            in_keyhole = True
                    
                    # Determine color for sample
                    col = None
                    if in_keyhole:
                        col = (15, 23, 42, 255)
                    elif in_cyan:
                        # Cyan Gradient
                        t_c = (py - 223) / 108.0
                        r_c = int(lerp(0x06, 0x02, t_c))
                        g_c = int(lerp(0xB6, 0x84, t_c))
                        b_c = int(lerp(0xD4, 0xC7, t_c))
                        col = (r_c, g_c, b_c, 255)
                    elif in_body or in_shackle:
                        col = (255, 255, 255, 255)
                    elif in_bezel:
                        t_bz = (px - 147 + py - 114) / 400.0
                        r_bz = int(lerp(0x60, 0x25, t_bz))
                        g_bz = int(lerp(0xA5, 0x63, t_bz))
                        b_bz = int(lerp(0xFA, 0xEB, t_bz))
                        col = (r_bz, g_bz, b_bz, 255)
                    elif in_main:
                        t_m = (py - 95) / 300.0
                        if t_m < 0.6:
                            tm2 = t_m / 0.6
                            r_m = int(lerp(0x3B, 0x1D, tm2))
                            g_m = int(lerp(0x82, 0x4E, tm2))
                            b_m = int(lerp(0xF6, 0xD8, tm2))
                        else:
                            tm2 = (t_m - 0.6) / 0.4
                            r_m = int(lerp(0x1D, 0x1E, tm2))
                            g_m = int(lerp(0x4E, 0x40, tm2))
                            b_m = int(lerp(0xD8, 0xAF, tm2))
                        col = (r_m, g_m, b_m, 255)
                    elif in_aura:
                        t_au = (py - 76) / 340.0
                        r_au = int(lerp(0x60, 0x25, t_au))
                        g_au = int(lerp(0xA5, 0x63, t_au))
                        b_au = int(lerp(0xFA, 0xEB, t_au))
                        col = (r_au, g_au, b_au, 120)
                    
                    if col is not None:
                        color_acc[0] += col[0] * (col[3] / 255.0)
                        color_acc[1] += col[1] * (col[3] / 255.0)
                        color_acc[2] += col[2] * (col[3] / 255.0)
                        color_acc[3] += col[3]
                        samples_cnt += 1
            
            if samples_cnt > 0:
                tot = SAMPLES * SAMPLES
                a_avg = color_acc[3] / tot
                if a_avg > 0:
                    fg_c = (
                        int(color_acc[0] / (color_acc[3] / 255.0) if color_acc[3] > 0 else 0),
                        int(color_acc[1] / (color_acc[3] / 255.0) if color_acc[3] > 0 else 0),
                        int(color_acc[2] / (color_acc[3] / 255.0) if color_acc[3] > 0 else 0),
                        int(a_avg)
                    )
                    set_pixel(x, y, fg_c)

print("Rendering high-res 512x512 logo...")
render_smooth()

png_data = create_png(WIDTH, HEIGHT, pixels)

# Save to store_assets and res/drawable
with open('store_assets/play_store_512x512_icon.png', 'wb') as f:
    f.write(png_data)

with open('app/src/main/res/drawable/play_store_logo_512.png', 'wb') as f:
    f.write(png_data)

print(f"Done! PNG generated successfully ({len(png_data)} bytes).")
