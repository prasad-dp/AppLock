import math
import struct
import zlib
import os

WIDTH = 1080
HEIGHT = 1920
OUTPUT_DIR = "store_assets/screenshots"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# 8x13 Simple Vector Font / Bitmap Glyph definition for clean rendering
# Basic 5x7 or 8x12 font for labels, titles, and numbers
# Even better: Procedural anti-aliased geometric font engine for smooth large text + crisp labels!

def create_png(width, height, pixels):
    raw_data = bytearray()
    row_bytes = width * 4
    for y in range(height):
        raw_data.append(0)  # Filter byte 0 (None)
        offset = y * row_bytes
        raw_data.extend(pixels[offset:offset + row_bytes])
    
    compressed = zlib.compress(raw_data, 6)
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

# Basic 5x7 Font Matrix for English ASCII (32-126)
FONT_5X7 = {
    ' ': [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00],
    '!': [0x04, 0x04, 0x04, 0x04, 0x00, 0x00, 0x04],
    '"': [0x0A, 0x0A, 0x0A, 0x00, 0x00, 0x00, 0x00],
    '#': [0x0A, 0x0A, 0x1F, 0x0A, 0x1F, 0x0A, 0x0A],
    '$': [0x04, 0x0F, 0x14, 0x0E, 0x05, 0x1E, 0x04],
    '%': [0x18, 0x19, 0x02, 0x04, 0x08, 0x13, 0x03],
    '&': [0x08, 0x14, 0x14, 0x08, 0x15, 0x12, 0x0D],
    '\'': [0x04, 0x04, 0x02, 0x00, 0x00, 0x00, 0x00],
    '(': [0x02, 0x04, 0x08, 0x08, 0x08, 0x04, 0x02],
    ')': [0x08, 0x04, 0x02, 0x02, 0x02, 0x04, 0x08],
    '*': [0x00, 0x04, 0x15, 0x0E, 0x15, 0x04, 0x00],
    '+': [0x00, 0x04, 0x04, 0x1F, 0x04, 0x04, 0x00],
    ',': [0x00, 0x00, 0x00, 0x00, 0x06, 0x06, 0x04],
    '-': [0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00],
    '.': [0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06],
    '/': [0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x00],
    '0': [0x0E, 0x11, 0x13, 0x15, 0x19, 0x11, 0x0E],
    '1': [0x04, 0x0C, 0x04, 0x04, 0x04, 0x04, 0x0E],
    '2': [0x0E, 0x11, 0x01, 0x02, 0x04, 0x08, 0x1F],
    '3': [0x1F, 0x02, 0x04, 0x02, 0x01, 0x11, 0x0E],
    '4': [0x02, 0x06, 0x0A, 0x12, 0x1F, 0x02, 0x02],
    '5': [0x1F, 0x10, 0x1E, 0x01, 0x01, 0x11, 0x0E],
    '6': [0x06, 0x08, 0x10, 0x1E, 0x11, 0x11, 0x0E],
    '7': [0x1F, 0x01, 0x02, 0x04, 0x08, 0x08, 0x08],
    '8': [0x0E, 0x11, 0x11, 0x0E, 0x11, 0x11, 0x0E],
    '9': [0x0E, 0x11, 0x11, 0x0F, 0x01, 0x02, 0x0C],
    ':': [0x00, 0x06, 0x06, 0x00, 0x06, 0x06, 0x00],
    ';': [0x00, 0x06, 0x06, 0x00, 0x06, 0x06, 0x04],
    '<': [0x02, 0x04, 0x08, 0x10, 0x08, 0x04, 0x02],
    '=': [0x00, 0x1F, 0x00, 0x1F, 0x00, 0x00, 0x00],
    '>': [0x08, 0x04, 0x02, 0x01, 0x02, 0x04, 0x08],
    '?': [0x0E, 0x11, 0x01, 0x02, 0x04, 0x00, 0x04],
    '@': [0x0E, 0x11, 0x01, 0x0D, 0x15, 0x15, 0x0E],
    'A': [0x0E, 0x11, 0x11, 0x1F, 0x11, 0x11, 0x11],
    'B': [0x1E, 0x11, 0x11, 0x1E, 0x11, 0x11, 0x1E],
    'C': [0x0E, 0x11, 0x10, 0x10, 0x10, 0x11, 0x0E],
    'D': [0x1C, 0x12, 0x11, 0x11, 0x11, 0x12, 0x1C],
    'E': [0x1F, 0x10, 0x10, 0x1E, 0x10, 0x10, 0x1F],
    'F': [0x1F, 0x10, 0x10, 0x1E, 0x10, 0x10, 0x10],
    'G': [0x0E, 0x11, 0x10, 0x17, 0x11, 0x11, 0x0F],
    'H': [0x11, 0x11, 0x11, 0x1F, 0x11, 0x11, 0x11],
    'I': [0x0E, 0x04, 0x04, 0x04, 0x04, 0x04, 0x0E],
    'J': [0x07, 0x02, 0x02, 0x02, 0x02, 0x12, 0x0C],
    'K': [0x11, 0x12, 0x14, 0x18, 0x14, 0x12, 0x11],
    'L': [0x10, 0x10, 0x10, 0x10, 0x10, 0x10, 0x1F],
    'M': [0x11, 0x1B, 0x15, 0x15, 0x11, 0x11, 0x11],
    'N': [0x11, 0x19, 0x15, 0x13, 0x11, 0x11, 0x11],
    'O': [0x0E, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0E],
    'P': [0x1E, 0x11, 0x11, 0x1E, 0x10, 0x10, 0x10],
    'Q': [0x0E, 0x11, 0x11, 0x11, 0x15, 0x12, 0x0D],
    'R': [0x1E, 0x11, 0x11, 0x1E, 0x14, 0x12, 0x11],
    'S': [0x0E, 0x11, 0x10, 0x0E, 0x01, 0x11, 0x0E],
    'T': [0x1F, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04],
    'U': [0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0E],
    'V': [0x11, 0x11, 0x11, 0x11, 0x11, 0x0A, 0x04],
    'W': [0x11, 0x11, 0x11, 0x15, 0x15, 0x1B, 0x11],
    'X': [0x11, 0x11, 0x0A, 0x04, 0x0A, 0x11, 0x11],
    'Y': [0x11, 0x11, 0x0A, 0x04, 0x04, 0x04, 0x04],
    'Z': [0x1F, 0x01, 0x02, 0x04, 0x08, 0x10, 0x1F],
    '[': [0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E],
    '\\': [0x00, 0x10, 0x08, 0x04, 0x02, 0x01, 0x00],
    ']': [0x0E, 0x02, 0x02, 0x02, 0x02, 0x02, 0x0E],
    '^': [0x04, 0x0A, 0x11, 0x00, 0x00, 0x00, 0x00],
    '_': [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1F],
    'a': [0x00, 0x00, 0x0E, 0x01, 0x0F, 0x11, 0x0F],
    'b': [0x10, 0x10, 0x16, 0x19, 0x11, 0x11, 0x1E],
    'c': [0x00, 0x00, 0x0E, 0x10, 0x10, 0x11, 0x0E],
    'd': [0x01, 0x01, 0x0D, 0x13, 0x11, 0x11, 0x0F],
    'e': [0x00, 0x00, 0x0E, 0x11, 0x1F, 0x10, 0x0E],
    'f': [0x06, 0x09, 0x08, 0x1C, 0x08, 0x08, 0x08],
    'g': [0x00, 0x0F, 0x11, 0x11, 0x0F, 0x01, 0x0E],
    'h': [0x10, 0x10, 0x16, 0x19, 0x11, 0x11, 0x11],
    'i': [0x04, 0x00, 0x0C, 0x04, 0x04, 0x04, 0x0E],
    'j': [0x02, 0x00, 0x06, 0x02, 0x02, 0x12, 0x0C],
    'k': [0x10, 0x10, 0x12, 0x14, 0x18, 0x14, 0x12],
    'l': [0x0C, 0x04, 0x04, 0x04, 0x04, 0x04, 0x0E],
    'm': [0x00, 0x00, 0x1A, 0x15, 0x15, 0x11, 0x11],
    'n': [0x00, 0x00, 0x16, 0x19, 0x11, 0x11, 0x11],
    'o': [0x00, 0x00, 0x0E, 0x11, 0x11, 0x11, 0x0E],
    'p': [0x00, 0x00, 0x1E, 0x11, 0x1E, 0x10, 0x10],
    'q': [0x00, 0x00, 0x0D, 0x13, 0x0F, 0x01, 0x01],
    'r': [0x00, 0x00, 0x16, 0x19, 0x10, 0x10, 0x10],
    's': [0x00, 0x00, 0x0F, 0x10, 0x0E, 0x01, 0x1E],
    't': [0x08, 0x08, 0x1C, 0x08, 0x08, 0x09, 0x06],
    'u': [0x00, 0x00, 0x11, 0x11, 0x11, 0x13, 0x0D],
    'v': [0x00, 0x00, 0x11, 0x11, 0x11, 0x0A, 0x04],
    'w': [0x00, 0x00, 0x11, 0x15, 0x15, 0x15, 0x0A],
    'x': [0x00, 0x00, 0x11, 0x0A, 0x04, 0x0A, 0x11],
    'y': [0x00, 0x00, 0x11, 0x11, 0x0F, 0x01, 0x0E],
    'z': [0x00, 0x00, 0x1F, 0x02, 0x04, 0x08, 0x1F],
    '|': [0x04, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04],
    '~': [0x00, 0x08, 0x15, 0x02, 0x00, 0x00, 0x00],
    '•': [0x00, 0x00, 0x0E, 0x0E, 0x0E, 0x00, 0x00],
}

class Canvas:
    def __init__(self, w, h):
        self.w = w
        self.h = h
        self.buf = bytearray(w * h * 4)
        
    def fill(self, r, g, b, a=255):
        row = bytearray([r, g, b, a] * self.w)
        for y in range(self.h):
            self.buf[y*self.w*4:(y+1)*self.w*4] = row
            
    def set_pixel(self, x, y, r, g, b, a=255):
        if 0 <= x < self.w and 0 <= y < self.h:
            idx = (y * self.w + x) * 4
            if a >= 255:
                self.buf[idx] = r
                self.buf[idx+1] = g
                self.buf[idx+2] = b
                self.buf[idx+3] = 255
            else:
                alpha = a / 255.0
                inv = 1.0 - alpha
                self.buf[idx] = int(r * alpha + self.buf[idx] * inv)
                self.buf[idx+1] = int(g * alpha + self.buf[idx+1] * inv)
                self.buf[idx+2] = int(b * alpha + self.buf[idx+2] * inv)
                self.buf[idx+3] = 255

    def draw_rect(self, x, y, w, h, r, g, b, a=255):
        x1 = max(0, int(x))
        y1 = max(0, int(y))
        x2 = min(self.w, int(x + w))
        y2 = min(self.h, int(y + h))
        for py in range(y1, y2):
            for px in range(x1, x2):
                self.set_pixel(px, py, r, g, b, a)

    def draw_rounded_rect(self, x, y, w, h, radius, r, g, b, a=255, border_w=0, br=0, bg_col=0, bb=0):
        x1 = max(0, int(x))
        y1 = max(0, int(y))
        x2 = min(self.w, int(x + w))
        y2 = min(self.h, int(y + h))
        rad = max(1, radius)
        
        for py in range(y1, y2):
            for px in range(x1, x2):
                # Check 4 corners
                dx = 0
                dy = 0
                in_corner = False
                if px < x + rad and py < y + rad: # Top left
                    dx = (x + rad) - px
                    dy = (y + rad) - py
                    in_corner = True
                elif px >= x + w - rad and py < y + rad: # Top right
                    dx = px - (x + w - rad - 1)
                    dy = (y + rad) - py
                    in_corner = True
                elif px < x + rad and py >= y + h - rad: # Bottom left
                    dx = (x + rad) - px
                    dy = py - (y + h - rad - 1)
                    in_corner = True
                elif px >= x + w - rad and py >= y + h - rad: # Bottom right
                    dx = px - (x + w - rad - 1)
                    dy = py - (y + h - rad - 1)
                    in_corner = True
                
                dist = math.sqrt(dx*dx + dy*dy) if in_corner else 0
                if dist > rad:
                    continue
                
                # Check border
                if border_w > 0:
                    is_border = False
                    if in_corner:
                        if dist >= rad - border_w:
                            is_border = True
                    else:
                        if px < x + border_w or px >= x + w - border_w or py < y + border_w or py >= y + h - border_w:
                            is_border = True
                    
                    if is_border:
                        self.set_pixel(px, py, br, bg_col, bb, a)
                        continue
                        
                self.set_pixel(px, py, r, g, b, a)

    def draw_circle(self, cx, cy, radius, r, g, b, a=255, stroke=0, sr=0, sg=0, sb=0):
        x1 = max(0, int(cx - radius - stroke))
        x2 = min(self.w, int(cx + radius + stroke + 1))
        y1 = max(0, int(cy - radius - stroke))
        y2 = min(self.h, int(cy + radius + stroke + 1))
        
        r2 = radius * radius
        out_r2 = (radius + stroke) * (radius + stroke)
        in_r2 = (radius - stroke) * (radius - stroke) if stroke > 0 else 0
        
        for py in range(y1, y2):
            dy = py - cy
            for px in range(x1, x2):
                dx = px - cx
                d2 = dx*dx + dy*dy
                if stroke > 0:
                    if in_r2 <= d2 <= out_r2:
                        self.set_pixel(px, py, sr, sg, sb, a)
                else:
                    if d2 <= r2:
                        self.set_pixel(px, py, r, g, b, a)

    def draw_text(self, text, start_x, start_y, scale, r, g, b, a=255, center=False):
        char_w = 6 * scale
        total_w = len(text) * char_w
        cur_x = int(start_x - total_w / 2) if center else int(start_x)
        cur_y = int(start_y)
        
        for ch in text:
            glyph = FONT_5X7.get(ch, FONT_5X7['?'])
            for row_idx, row_byte in enumerate(glyph):
                for col_idx in range(5):
                    if (row_byte >> (4 - col_idx)) & 1:
                        # Draw scaled block
                        px = cur_x + col_idx * scale
                        py = cur_y + row_idx * scale
                        for dy in range(scale):
                            for dx in range(scale):
                                self.set_pixel(px + dx, py + dy, r, g, b, a)
            cur_x += char_w

def apply_background(canvas, accent_rgb):
    # Dark Slate Gradient + Accent Radial Glow
    ar, ag, ab = accent_rgb
    for y in range(HEIGHT):
        ny = y / HEIGHT
        # Base slate vertical gradient
        base_r = int(8 + (15 - 8) * ny)
        base_g = int(12 + (23 - 12) * ny)
        base_b = int(20 + (42 - 20) * ny)
        
        # Radial Top Glow
        dy = (y - 300) / 450.0
        for x in range(WIDTH):
            dx = (x - 540) / 420.0
            dist = math.sqrt(dx*dx + dy*dy)
            glow = math.exp(-dist * 1.5) * 0.45
            
            r = min(255, int(base_r + ar * glow))
            g = min(255, int(base_g + ag * glow))
            b = min(255, int(base_b + ab * glow))
            
            idx = (y * WIDTH + x) * 4
            canvas.buf[idx] = r
            canvas.buf[idx+1] = g
            canvas.buf[idx+2] = b
            canvas.buf[idx+3] = 255

def draw_phone_frame(canvas, accent_rgb):
    # Phone Chassis Mockup (x=160, y=520, w=760, h=1350, r=60)
    px, py, pw, ph = 160, 520, 760, 1350
    # Outer dark chassis with subtle glowing rim
    ar, ag, ab = accent_rgb
    canvas.draw_rounded_rect(px, py, pw, ph, 60, 15, 23, 42, border_w=5, br=ar, bg_col=ag, bb=ab)
    # Inner Bezel
    canvas.draw_rounded_rect(px+12, py+12, pw-24, ph-24, 52, 0, 0, 0)
    # Screen glass canvas
    canvas.draw_rounded_rect(px+20, py+20, pw-40, ph-40, 46, 11, 15, 25)
    
    # Camera Punch Hole
    canvas.draw_circle(px + pw//2, py + 55, 12, 0, 0, 0, stroke=2, sr=30, sg=41, sb=59)
    
    # Status Bar: 09:41, 5G, Battery
    canvas.draw_text("09:41", px + 60, py + 48, 3, 148, 163, 184)
    canvas.draw_text("5G  100%", px + pw - 180, py + 48, 3, 148, 163, 184)
    
    # Bottom Home pill
    canvas.draw_rounded_rect(px + pw//2 - 90, py + ph - 45, 180, 8, 4, 100, 116, 139)

def draw_header_section(canvas, badge, title, subtitle, highlights, accent_rgb):
    ar, ag, ab = accent_rgb
    
    # 1. Feature Badge Pill (Top Center)
    badge_w = len(badge) * 16 + 60
    canvas.draw_rounded_rect(540 - badge_w//2, 110, badge_w, 60, 30, 15, 23, 42, border_w=3, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text(badge, 540, 128, 4, ar, ag, ab, center=True)
    
    # 2. Main Title (Big Bold Header)
    canvas.draw_text(title, 540, 220, 8, 255, 255, 255, center=True)
    
    # 3. Subtitle
    canvas.draw_text(subtitle, 540, 310, 4, 148, 163, 184, center=True)
    
    # 4. Highlight Tags (3 pills)
    total_hl_w = 720
    tag_w = 230
    gap = (total_hl_w - 3*tag_w) // 2
    for i, hl in enumerate(highlights):
        tx = 180 + i * (tag_w + gap)
        canvas.draw_rounded_rect(tx, 390, tag_w, 54, 27, 30, 41, 59, border_w=2, br=ar, bg_col=ag, bb=ab)
        canvas.draw_text(hl, tx + tag_w//2, 408, 3, 248, 250, 252, center=True)

# ----------------- SCREEN TYPE DRAWERS -----------------

def draw_screen_instant_lock(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # App Logo Header (WhatsApp Lock)
    canvas.draw_circle(540, py + 180, 50, 37, 211, 102)
    canvas.draw_text("WhatsApp", 540, py + 260, 5, 248, 250, 252, center=True)
    canvas.draw_text("Protected by App Locker Pro", 540, py + 310, 3, 148, 163, 184, center=True)
    
    # PIN Indicators (4 circles)
    for i in range(4):
        cx = 540 - 90 + i * 60
        if i < 3:
            canvas.draw_circle(cx, py + 380, 16, ar, ag, ab)
        else:
            canvas.draw_circle(cx, py + 380, 16, 30, 41, 59, stroke=2, sr=71, sg=85, sb=105)
            
    # Keypad Grid (1-9, Bio, 0, Back)
    keys = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "DEL"]
    for row in range(4):
        for col in range(3):
            k_idx = row * 3 + col
            kx = 250 + col * 180
            ky = py + 450 + row * 130
            val = keys[k_idx]
            if val == "BIO":
                canvas.draw_rounded_rect(kx, ky, 150, 100, 28, 15, 23, 42, border_w=2, br=ar, bg_col=ag, bb=ab)
                canvas.draw_text("FINGER", kx + 75, ky + 40, 3, ar, ag, ab, center=True)
            elif val == "DEL":
                canvas.draw_rounded_rect(kx, ky, 150, 100, 28, 15, 23, 42)
                canvas.draw_text("DEL", kx + 75, ky + 40, 3, 239, 68, 68, center=True)
            else:
                canvas.draw_rounded_rect(kx, ky, 150, 100, 28, 30, 41, 59)
                canvas.draw_text(val, kx + 75, ky + 35, 5, 248, 250, 252, center=True)
                
    # Latency Guarantee Tag
    canvas.draw_rounded_rect(260, py + 1050, 560, 70, 20, 15, 23, 42, border_w=2, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text("0ms Window Interception Active", 540, py + 1072, 4, ar, ag, ab, center=True)

def draw_screen_intruder_selfie(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # Intruder Alert Banner
    canvas.draw_rounded_rect(240, py + 140, 600, 110, 24, 40, 15, 20, border_w=3, br=239, bg_col=68, bb=68)
    canvas.draw_circle(300, py + 195, 32, 239, 68, 68)
    canvas.draw_text("!", 300, py + 180, 6, 255, 255, 255, center=True)
    canvas.draw_text("INTRUDER DETECTED", 360, py + 172, 4, 252, 165, 165)
    canvas.draw_text("3 Failed Attempts on Private Gallery", 360, py + 210, 3, 203, 213, 225)
    
    # Viewfinder Preview Box
    canvas.draw_rounded_rect(260, py + 290, 560, 520, 32, 15, 23, 42, border_w=3, br=239, bg_col=68, bb=68)
    # Camera crosshair
    canvas.draw_circle(540, py + 500, 90, 30, 41, 59, stroke=4, sr=239, sg=68, sb=68)
    canvas.draw_circle(540, py + 460, 36, 71, 85, 105)
    canvas.draw_rounded_rect(480, py + 510, 120, 70, 35, 71, 85, 105)
    
    # Badge
    canvas.draw_rounded_rect(340, py + 640, 400, 60, 30, 239, 68, 68)
    canvas.draw_text("SILENT FRONT SELFIE SAVED", 540, py + 660, 3, 255, 255, 255, center=True)
    canvas.draw_text("Hardware AES-256 Encrypted", 540, py + 740, 4, 248, 250, 252, center=True)
    canvas.draw_text("Aug 18, 2026 - 09:41:22 PM", 540, py + 775, 3, 148, 163, 184, center=True)

    # Policy card
    canvas.draw_rounded_rect(240, py + 860, 600, 220, 24, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Intruder Capture Rules", 270, py + 890, 4, 248, 250, 252)
    canvas.draw_text("Failed attempts limit: 1-5 Tries", 270, py + 940, 3, 148, 163, 184)
    canvas.draw_text("Silent Mode: No Flash or Audio", 270, py + 980, 3, 16, 185, 129)
    canvas.draw_text("Encryption: Hardware KeyStore GCM", 270, py + 1020, 3, 6, 182, 212)

def draw_screen_vault_shield(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # Header bar
    canvas.draw_rounded_rect(240, py + 130, 600, 80, 20, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Intruder Evidence Logs", 270, py + 158, 4, 248, 250, 252)
    canvas.draw_rounded_rect(650, py + 145, 160, 50, 16, 6, 182, 212)
    canvas.draw_text("SHIELDED", 730, py + 162, 3, 255, 255, 255, center=True)
    
    # Log 1 (Frosted blurred card)
    canvas.draw_rounded_rect(240, py + 240, 600, 250, 28, 19, 27, 46, border_w=3, br=ar, bg_col=ag, bb=ab)
    # Frosted image box
    canvas.draw_rounded_rect(265, py + 265, 200, 200, 20, 30, 41, 59)
    canvas.draw_circle(365, py + 350, 45, 15, 23, 42, stroke=3, sr=ar, sg=ag, sb=ab)
    canvas.draw_text("TAP TO", 365, py + 415, 2, ar, ag, ab, center=True)
    canvas.draw_text("REVEAL", 365, py + 435, 2, ar, ag, ab, center=True)
    
    canvas.draw_text("WhatsApp Snooper", 490, py + 285, 4, 248, 250, 252)
    canvas.draw_text("2 Failed PIN Attempts", 490, py + 335, 3, 239, 68, 68)
    canvas.draw_text("Today, 09:41 PM", 490, py + 375, 3, 148, 163, 184)
    canvas.draw_rounded_rect(490, py + 415, 180, 40, 12, 30, 41, 59)
    canvas.draw_text("AES-256 .ENC", 580, py + 428, 2, 100, 116, 139, center=True)

    # Log 2
    canvas.draw_rounded_rect(240, py + 520, 600, 250, 28, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_rounded_rect(265, py + 545, 200, 200, 20, 30, 41, 59)
    canvas.draw_circle(365, py + 630, 45, 15, 23, 42, stroke=3, sr=ar, sg=ag, sb=ab)
    canvas.draw_text("TAP TO", 365, py + 695, 2, ar, ag, ab, center=True)
    canvas.draw_text("REVEAL", 365, py + 715, 2, ar, ag, ab, center=True)
    
    canvas.draw_text("Photos & Media", 490, py + 565, 4, 248, 250, 252)
    canvas.draw_text("Pattern Mismatch", 490, py + 615, 3, 239, 68, 68)
    canvas.draw_text("Yesterday, 04:15 PM", 490, py + 655, 3, 148, 163, 184)
    canvas.draw_rounded_rect(490, py + 695, 180, 40, 12, 30, 41, 59)
    canvas.draw_text("AES-256 .ENC", 580, py + 708, 2, 100, 116, 139, center=True)

    # Biometric Unlock Button
    canvas.draw_rounded_rect(240, py + 820, 600, 90, 28, 6, 182, 212)
    canvas.draw_text("UNLOCK VAULT WITH FINGERPRINT", 540, py + 855, 4, 255, 255, 255, center=True)

def draw_screen_encryption_hub(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # Shield graphic
    canvas.draw_circle(540, py + 180, 70, 16, 185, 129, stroke=4, sr=16, sg=185, sb=129)
    canvas.draw_text("KEYSTORE", 540, py + 172, 3, 255, 255, 255, center=True)
    canvas.draw_text("Hardware Encryption Engine", 540, py + 280, 4, 248, 250, 252, center=True)
    canvas.draw_text("Android KeyStore - AES-256 GCM", 540, py + 325, 3, 16, 185, 129, center=True)

    # Specs Card 1
    canvas.draw_rounded_rect(240, py + 390, 600, 160, 24, 19, 27, 46, border_w=2, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text("Hardware Security Module", 270, py + 420, 4, 248, 250, 252)
    canvas.draw_text("Keys generated inside Secure Enclave (TEE)", 270, py + 465, 3, 148, 163, 184)
    canvas.draw_text("StrongBox Hardware Backed", 270, py + 505, 3, 16, 185, 129)

    # Specs Card 2
    canvas.draw_rounded_rect(240, py + 580, 600, 160, 24, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Direct In-Memory Ciphers", 270, py + 610, 4, 248, 250, 252)
    canvas.draw_text("Zero unencrypted raw snapshots touch disk", 270, py + 655, 3, 148, 163, 184)
    canvas.draw_text("0 Bytes Leakage Guarantee", 270, py + 695, 3, 16, 185, 129)

    # Specs Card 3
    canvas.draw_rounded_rect(240, py + 770, 600, 160, 24, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Zero Remote Cloud Sync", 270, py + 800, 4, 248, 250, 252)
    canvas.draw_text("100% Local Sandboxed Persistence", 270, py + 845, 3, 148, 163, 184)
    canvas.draw_text("Air-Gapped Offline Protection", 270, py + 885, 3, 16, 185, 129)

def draw_screen_lock_modes(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    canvas.draw_text("Choose Master Passcode", 540, py + 140, 5, 248, 250, 252, center=True)
    canvas.draw_text("Flexible security styles for all apps", 540, py + 190, 3, 148, 163, 184, center=True)

    # Mode 1: PIN
    canvas.draw_rounded_rect(240, py + 240, 600, 140, 24, 19, 27, 46, border_w=3, br=ar, bg_col=ag, bb=ab)
    canvas.draw_rounded_rect(265, py + 265, 80, 80, 20, 139, 92, 246)
    canvas.draw_text("PIN", 305, py + 295, 3, 255, 255, 255, center=True)
    canvas.draw_text("4 or 6-Digit PIN", 370, py + 280, 4, 248, 250, 252)
    canvas.draw_text("Fast tactile number pad with haptics", 370, py + 325, 3, 148, 163, 184)

    # Mode 2: Pattern Grid
    canvas.draw_rounded_rect(240, py + 410, 600, 360, 28, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("3x3 Pattern Matrix", 270, py + 445, 4, 248, 250, 252)
    # 3x3 Dots
    for r in range(3):
        for c in range(3):
            cx = 440 + c * 100
            cy = py + 520 + r * 100
            if (r == 0 and c < 2) or (r == 1 and c == 2) or (r == 2 and c == 1):
                canvas.draw_circle(cx, cy, 18, ar, ag, ab)
            else:
                canvas.draw_circle(cx, cy, 14, 71, 85, 105)
                
    # Mode 3: Biometrics
    canvas.draw_rounded_rect(240, py + 800, 600, 140, 24, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_rounded_rect(265, py + 825, 80, 80, 20, 139, 92, 246)
    canvas.draw_text("BIO", 305, py + 855, 3, 255, 255, 255, center=True)
    canvas.draw_text("Biometric Touch / Face", 370, py + 840, 4, 248, 250, 252)
    canvas.draw_text("Unlock with native device biometric sensor", 370, py + 885, 3, 148, 163, 184)

def draw_screen_double_lock(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # Banner
    canvas.draw_rounded_rect(240, py + 130, 600, 140, 24, 45, 35, 15, border_w=3, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text("Smart Double-Lock Advisor", 270, py + 165, 4, 252, 211, 77)
    canvas.draw_text("Detected apps with native security:", 270, py + 205, 3, 148, 163, 184)
    canvas.draw_text("Auto loop-prevention enabled", 270, py + 235, 3, 16, 185, 129)

    # App 1
    canvas.draw_rounded_rect(240, py + 300, 600, 130, 22, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_circle(290, py + 365, 30, 37, 211, 102)
    canvas.draw_text("WhatsApp", 340, py + 345, 4, 248, 250, 252)
    canvas.draw_text("Has native fingerprint lock", 340, py + 385, 3, 148, 163, 184)
    # Switch
    canvas.draw_rounded_rect(720, py + 340, 90, 50, 25, ar, ag, ab)
    canvas.draw_circle(785, py + 365, 20, 255, 255, 255)

    # App 2
    canvas.draw_rounded_rect(240, py + 460, 600, 130, 22, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_circle(290, py + 525, 30, 59, 130, 246)
    canvas.draw_text("Google Wallet / Banks", 340, py + 505, 4, 248, 250, 252)
    canvas.draw_text("Payment biometric security", 340, py + 545, 3, 148, 163, 184)
    canvas.draw_rounded_rect(720, py + 500, 90, 50, 25, ar, ag, ab)
    canvas.draw_circle(785, py + 525, 20, 255, 255, 255)

    # Delay selector
    canvas.draw_rounded_rect(240, py + 620, 600, 220, 24, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Re-Lock Delay Grace Period", 270, py + 655, 4, 248, 250, 252)
    canvas.draw_text("Prevents instant re-lock on quick switching", 270, py + 695, 3, 148, 163, 184)
    
    delays = ["0s", "15s", "30s", "1m"]
    for i, d in enumerate(delays):
        bx = 270 + i * 135
        if i == 1:
            canvas.draw_rounded_rect(bx, py + 740, 120, 60, 16, ar, ag, ab)
            canvas.draw_text(d, bx + 60, py + 760, 4, 0, 0, 0, center=True)
        else:
            canvas.draw_rounded_rect(bx, py + 740, 120, 60, 16, 30, 41, 59)
            canvas.draw_text(d, bx + 60, py + 760, 4, 148, 163, 184, center=True)

def draw_screen_shredder(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    canvas.draw_circle(540, py + 180, 60, 236, 72, 153, stroke=4, sr=236, sg=72, sb=153)
    canvas.draw_text("WIPE", 540, py + 172, 3, 255, 255, 255, center=True)
    canvas.draw_text("Forensic Storage Shredder", 540, py + 270, 4, 248, 250, 252, center=True)
    canvas.draw_text("DoD 5220.22-M Multi-Pass Purge", 540, py + 310, 3, 236, 72, 153, center=True)

    # Progress Card
    canvas.draw_rounded_rect(240, py + 370, 600, 460, 28, 19, 27, 46, border_w=3, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text("Permanently Shredding File", 270, py + 410, 4, 248, 250, 252)
    canvas.draw_text("Target: intruder_alert_2026.enc", 270, py + 450, 3, 148, 163, 184)

    # Pass 1
    canvas.draw_circle(290, py + 510, 14, 16, 185, 129)
    canvas.draw_text("Pass 1: Overwrite Cryptographic Random Bytes", 320, py + 500, 3, 248, 250, 252)

    # Pass 2
    canvas.draw_circle(290, py + 560, 14, 16, 185, 129)
    canvas.draw_text("Pass 2: Overwrite Bitwise Inverse (0xFF)", 320, py + 550, 3, 248, 250, 252)

    # Pass 3
    canvas.draw_circle(290, py + 610, 14, ar, ag, ab)
    canvas.draw_text("Pass 3: Overwrite Zeros (0x00)", 320, py + 600, 3, ar, ag, ab)

    # Progress bar
    canvas.draw_rounded_rect(270, py + 670, 540, 24, 12, 30, 41, 59)
    canvas.draw_rounded_rect(270, py + 670, 480, 24, 12, ar, ag, ab)
    canvas.draw_text("92% Completed", 540, py + 720, 3, ar, ag, ab, center=True)

    canvas.draw_rounded_rect(270, py + 760, 540, 50, 16, 30, 41, 59)
    canvas.draw_text("Zero Forensic Recovery Possible", 540, py + 776, 3, 16, 185, 129, center=True)

def draw_screen_security_hub(canvas, accent_rgb):
    px, py = 180, 540
    ar, ag, ab = accent_rgb
    
    # Active protection status card
    canvas.draw_rounded_rect(240, py + 130, 600, 130, 24, 30, 27, 75, border_w=3, br=ar, bg_col=ag, bb=ab)
    canvas.draw_text("Active Protection", 270, py + 165, 4, 248, 250, 252)
    canvas.draw_text("18 Apps Locked - Zero Delay Mode", 270, py + 210, 3, 165, 180, 252)
    canvas.draw_rounded_rect(720, py + 165, 90, 50, 25, 16, 185, 129)
    canvas.draw_circle(785, py + 190, 20, 255, 255, 255)

    # Search Bar
    canvas.draw_rounded_rect(240, py + 280, 600, 65, 20, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
    canvas.draw_text("Search apps to lock...", 270, py + 300, 3, 100, 116, 139)

    # App list items
    apps = [
        ("WhatsApp", "Social - Instant Lock", (37, 211, 102)),
        ("Google Photos", "Gallery - Intruder Alert ON", (234, 67, 53)),
        ("System Settings", "Prevents App Uninstallation", (71, 85, 105)),
        ("Google Play Store", "Blocks New Installations", (59, 130, 246)),
    ]
    for i, (app_name, sub, col) in enumerate(apps):
        ay = py + 370 + i * 115
        canvas.draw_rounded_rect(240, ay, 600, 100, 20, 19, 27, 46, border_w=2, br=30, bg_col=41, bb=59)
        cr, cg, cb = col
        canvas.draw_circle(290, ay + 50, 24, cr, cg, cb)
        canvas.draw_text(app_name, 335, ay + 32, 4, 248, 250, 252)
        canvas.draw_text(sub, 335, ay + 65, 3, 148, 163, 184)
        canvas.draw_circle(775, ay + 50, 20, ar, ag, ab)
        canvas.draw_text("L", 775, ay + 42, 2, 255, 255, 255, center=True)

SCREEN_RENDERERS = {
    "lock_screen": draw_screen_instant_lock,
    "intruder_capture": draw_screen_intruder_selfie,
    "vault_shield": draw_screen_vault_shield,
    "encryption_hub": draw_screen_encryption_hub,
    "lock_modes": draw_screen_lock_modes,
    "double_lock": draw_screen_double_lock,
    "shredder": draw_screen_shredder,
    "security_hub": draw_screen_security_hub,
}

SCREENSHOT_METADATA = [
    {
        "id": "1_instant_lock",
        "badge": "0ms ZERO-DELAY ENGINE",
        "title": "0ms Instant App Lock",
        "subtitle": "Blocks WhatsApp & Banking with zero screen glimpse",
        "highlights": ["Zero Latency", "Accessibility", "No Glimpse"],
        "screen_type": "lock_screen",
        "accent_rgb": (59, 130, 246),
    },
    {
        "id": "2_intruder_selfie",
        "badge": "SILENT INTRUDER CAMERA",
        "title": "Silent Intruder Selfie",
        "subtitle": "Silently captures snooper photos on incorrect passcode",
        "highlights": ["Zero Shutter", "No Flash", "Instant Capture"],
        "screen_type": "intruder_capture",
        "accent_rgb": (239, 68, 68),
    },
    {
        "id": "3_biometric_vault",
        "badge": "TAP TO REVEAL VAULT",
        "title": "Biometric Shield Vault",
        "subtitle": "Intruder evidence stays frosted until fingerprint unlock",
        "highlights": ["Frosted Previews", "Fingerprint / Face", "100% On-Device"],
        "screen_type": "vault_shield",
        "accent_rgb": (6, 182, 212),
    },
    {
        "id": "4_hardware_encryption",
        "badge": "HARDWARE KEYSTORE AES-256",
        "title": "Military AES-256 GCM",
        "subtitle": "Hardware-backed Android KeyStore encryption on disk",
        "highlights": ["AES-256 GCM", "Android KeyStore", "Zero Cloud Upload"],
        "screen_type": "encryption_hub",
        "accent_rgb": (16, 185, 129),
    },
    {
        "id": "5_lock_modes",
        "badge": "MULTIPLE PASSCODE STYLES",
        "title": "PIN, Pattern & Password",
        "subtitle": "Flexible locking options with tactile feedback & biometrics",
        "highlights": ["4/6-Digit PIN", "3x3 Pattern", "Alphanumeric"],
        "screen_type": "lock_modes",
        "accent_rgb": (139, 92, 246),
    },
    {
        "id": "6_double_lock_advisor",
        "badge": "DUAL-LOCK LOOP PREVENTER",
        "title": "Smart Double-Lock Advisor",
        "subtitle": "Eliminates frustrating double-auth on banking & chats",
        "highlights": ["Loop Prevention", "Banking Optimized", "Smart Delay"],
        "screen_type": "double_lock",
        "accent_rgb": (245, 158, 11),
    },
    {
        "id": "7_forensic_shredder",
        "badge": "3-PASS PERMANENT SHREDDER",
        "title": "3-Pass Forensic Shredder",
        "subtitle": "Overwrites storage sectors with random noise & zeros",
        "highlights": ["3-Pass DoD Wipe", "Zero Recovery", "Permanent Purge"],
        "screen_type": "shredder",
        "accent_rgb": (236, 72, 153),
    },
    {
        "id": "8_security_hub",
        "badge": "COMPLETE COMMAND CENTER",
        "title": "Privacy Command Center",
        "subtitle": "Per-app re-lock timers, camouflage & dark Material 3 UI",
        "highlights": ["Re-Lock Timers", "All Apps Locked", "Fluid M3 Theme"],
        "screen_type": "security_hub",
        "accent_rgb": (99, 102, 241),
    },
]

def main():
    print("Generating 8 Full-Resolution (1080x1920) Play Store Screenshots via Pure Python Engine...")
    for idx, meta in enumerate(SCREENSHOT_METADATA, 1):
        canvas = Canvas(WIDTH, HEIGHT)
        accent = meta["accent_rgb"]
        
        # 1. Background & Glow
        apply_background(canvas, accent)
        
        # 2. Header Content
        draw_header_section(canvas, meta["badge"], meta["title"], meta["subtitle"], meta["highlights"], accent)
        
        # 3. Device Mockup Frame
        draw_phone_frame(canvas, accent)
        
        # 4. In-App Screen Content
        renderer = SCREEN_RENDERERS[meta["screen_type"]]
        renderer(canvas, accent)
        
        # 5. Save PNG
        out_path = f"{OUTPUT_DIR}/playstore_phone_screenshot_{meta['id']}.png"
        png_bytes = create_png(WIDTH, HEIGHT, canvas.buf)
        with open(out_path, "wb") as f:
            f.write(png_bytes)
        print(f"[{idx}/8] Successfully generated: {out_path} ({len(png_bytes)//1024} KB)")

    print("\nAll 8 Play Store phone screenshots generated successfully in store_assets/screenshots/")

if __name__ == "__main__":
    main()
