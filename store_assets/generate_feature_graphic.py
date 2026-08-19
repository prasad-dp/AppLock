import math
import struct
import zlib

WIDTH = 1024
HEIGHT = 500

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

def lerp(a, b, t):
    return a + (b - a) * max(0.0, min(1.0, t))

def blend_color(bg, fg):
    a_fg = fg[3] / 255.0
    if a_fg <= 0: return bg
    if a_fg >= 1: return fg
    a_bg = bg[3] / 255.0
    a_out = a_fg + a_bg * (1 - a_fg)
    if a_out <= 0: return (0, 0, 0, 0)
    r = int((fg[0] * a_fg + bg[0] * a_bg * (1 - a_fg)) / a_out)
    g = int((fg[1] * a_fg + bg[1] * a_bg * (1 - a_fg)) / a_out)
    b = int((fg[2] * a_fg + bg[2] * a_bg * (1 - a_fg)) / a_out)
    a = int(a_out * 255)
    return (r, g, b, a)

def main():
    pixels = bytearray(WIDTH * HEIGHT * 4)
    
    # 1. Background gradient (Rich Deep Space Navy / Slate with subtle radial glow)
    for y in range(HEIGHT):
        ny = y / HEIGHT
        for x in range(WIDTH):
            nx = x / WIDTH
            
            # Base background dark gradient
            r = int(lerp(11, 20, ny))
            g = int(lerp(15, 32, ny))
            b = int(lerp(25, 55, ny))
            
            # Radial Glow 1 (Center Left - Cyan/Blue)
            dx1 = (x - 300) / 280.0
            dy1 = (y - 250) / 200.0
            dist1 = math.sqrt(dx1*dx1 + dy1*dy1)
            glow1 = math.exp(-dist1 * 1.6)
            
            # Radial Glow 2 (Top Right - Purple/Indigo)
            dx2 = (x - 850) / 320.0
            dy2 = (y - 120) / 220.0
            dist2 = math.sqrt(dx2*dx2 + dy2*dy2)
            glow2 = math.exp(-dist2 * 2.0)
            
            r = min(255, int(r + glow1 * 25 + glow2 * 45))
            g = min(255, int(g + glow1 * 75 + glow2 * 25))
            b = min(255, int(b + glow1 * 160 + glow2 * 90))
            
            idx = (y * WIDTH + x) * 4
            pixels[idx] = r
            pixels[idx+1] = g
            pixels[idx+2] = b
            pixels[idx+3] = 255

    # 2. Draw Shield Symbol on Left Side (center_x=280, center_y=250, size=150)
    scx, scy = 260, 250
    for y in range(HEIGHT):
        for x in range(WIDTH):
            dx = (x - scx) / 140.0
            dy = (y - scy) / 140.0
            
            # Shield equation
            # Upper curved flat, sides tapering down to point
            if -1.0 <= dx <= 1.0 and -1.0 <= dy <= 1.1:
                # Top arch
                top_boundary = -0.85 + (dx * dx) * 0.15
                # Bottom taper
                bottom_boundary = 0.2 + (abs(dx)) * 0.9
                
                if top_boundary <= dy <= bottom_boundary:
                    # Inside shield
                    # Distance to edge for bevel/glow
                    edge_dist = min(dy - top_boundary, bottom_boundary - dy, 1.0 - abs(dx))
                    edge_norm = max(0.0, min(1.0, edge_dist * 8.0))
                    
                    # Gradient across shield (Vibrant Cyan to Royal Blue)
                    t_shield = (dx + dy + 1.5) / 3.0
                    sr = int(lerp(14, 59, t_shield))
                    sg = int(lerp(165, 130, t_shield))
                    sb = int(lerp(233, 246, t_shield))
                    
                    # Outer border highlight
                    if edge_norm < 0.2:
                        sr, sg, sb = 224, 242, 254
                        
                    idx = (y * WIDTH + x) * 4
                    pixels[idx] = int(lerp(pixels[idx], sr, edge_norm))
                    pixels[idx+1] = int(lerp(pixels[idx+1], sg, edge_norm))
                    pixels[idx+2] = int(lerp(pixels[idx+2], sb, edge_norm))

    # 3. Draw Padlock on Shield (center_x=260, center_y=250)
    lcx, lcy = 260, 250
    for y in range(HEIGHT):
        for x in range(WIDTH):
            # Padlock shackle
            sdx = (x - lcx)
            sdy = (y - (lcy - 30))
            dist_shackle = math.sqrt(sdx*sdx + sdy*sdy)
            if 20 <= dist_shackle <= 32 and y <= lcy - 12:
                idx = (y * WIDTH + x) * 4
                pixels[idx] = 255
                pixels[idx+1] = 255
                pixels[idx+2] = 255

            # Padlock body (rounded rect)
            if abs(x - lcx) <= 38 and abs(y - (lcy + 15)) <= 28:
                idx = (y * WIDTH + x) * 4
                pixels[idx] = 255
                pixels[idx+1] = 255
                pixels[idx+2] = 255
                
            # Keyhole
            kdx = abs(x - lcx)
            kdy = y - (lcy + 10)
            if (kdx*kdx + kdy*kdy <= 36) or (kdx <= 3 and 0 <= kdy <= 16):
                idx = (y * WIDTH + x) * 4
                pixels[idx] = 15
                pixels[idx+1] = 23
                pixels[idx+2] = 42

    # Save output
    png_bytes = create_png(WIDTH, HEIGHT, pixels)
    with open("store_assets/play_store_1024x500_feature_graphic.png", "wb") as f:
        f.write(png_bytes)
    print("Feature Graphic generated successfully: store_assets/play_store_1024x500_feature_graphic.png")

if __name__ == "__main__":
    main()
