"""
Generate 6 100% Fully-Packed, High-Conversion Google Play Store Screenshots for App Locker.
- Authentic Jetpack Compose Dark UI matching the real app.
- Full-bleed device frame extending to the bottom edge of the poster (Apple/Play Store studio style).
- 100% filled phone screen from top status bar down to bottom navigation tab bar and gesture indicator.
- Zero empty space, zero half-filled voids, zero floating shoulder badges.
- Strictly 24-bit RGB PNG (no alpha channel), 1080 x 1920 pixels, < 8 MB.
"""

import os
import subprocess
from pathlib import Path
from PIL import Image

OUTPUT_DIR = Path("D:/app-locker/store_assets/screenshots")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

EDGE_PATH = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"

COMMON_CSS = """
<head>
<meta charset="UTF-8">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
<style>
  * {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    user-select: none;
    -webkit-font-smoothing: antialiased;
  }
  
  body {
    width: 1080px;
    height: 1920px;
    overflow: hidden;
    background: #060911;
    font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  }
  
  .canvas {
    position: relative;
    width: 1080px;
    height: 1920px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24px 36px 24px;
  }
  
  /* Studio Subtle Grid */
  .grid-pattern {
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(to right, rgba(255, 255, 255, 0.025) 1px, transparent 1px),
      linear-gradient(to bottom, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
    background-size: 40px 40px;
    pointer-events: none;
    z-index: 1;
  }
  
  /* Ambient Radial Backlights */
  .glow-top {
    position: absolute;
    top: -140px;
    left: 50%;
    transform: translateX(-50%);
    width: 900px;
    height: 520px;
    border-radius: 50%;
    filter: blur(140px);
    opacity: 0.55;
    pointer-events: none;
    z-index: 2;
  }
  
  /* Bottom Ambient Glow */
  .glow-bottom {
    position: absolute;
    bottom: -200px;
    left: 50%;
    transform: translateX(-50%);
    width: 800px;
    height: 400px;
    border-radius: 50%;
    filter: blur(160px);
    opacity: 0.25;
    pointer-events: none;
    z-index: 2;
  }
  
  /* Subtle Premium Noise Overlay */
  .noise-overlay {
    position: absolute;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
    opacity: 0.4;
    pointer-events: none;
    z-index: 3;
  }
  
  /* Feature Category Pill */
  .category-pill {
    position: relative;
    z-index: 10;
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 20px;
    border-radius: 999px;
    font-size: 12.5px;
    font-weight: 800;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    backdrop-filter: blur(20px);
    margin-bottom: 6px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
  }
  
  /* Hero Typography */
  .hero-title {
    position: relative;
    z-index: 10;
    font-size: 44px;
    font-weight: 900;
    text-align: center;
    line-height: 1.1;
    letter-spacing: -0.035em;
    max-width: 980px;
    margin-bottom: 5px;
    text-shadow: 0 6px 24px rgba(0, 0, 0, 0.7);
  }
  
  .hero-subtitle {
    position: relative;
    z-index: 10;
    font-size: 17px;
    font-weight: 500;
    color: #94A3B8;
    text-align: center;
    max-width: 880px;
    line-height: 1.28;
    letter-spacing: -0.01em;
    margin-bottom: 12px;
  }
  
  /* Phone Frame Container: Fully contained within canvas */
  .device-stage {
    position: relative;
    z-index: 10;
    width: 870px;
    flex: 1;
    display: flex;
    justify-content: center;
    min-height: 0;
  }
  
  .phone-frame {
    position: relative;
    width: 860px;
    height: 100%;
    background: #111624;
    border-radius: 54px;
    padding: 13px;
    box-shadow: 
      0 0 0 3px rgba(255, 255, 255, 0.2),
      0 0 0 8px #1A2234,
      0 35px 90px -15px rgba(0, 0, 0, 0.95),
      0 15px 45px rgba(0, 0, 0, 0.8);
    overflow: hidden;
  }
  
  /* Specular Highlight Rim */
  .phone-frame::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 54px;
    padding: 2.5px;
    background: linear-gradient(135deg, rgba(255,255,255,0.5) 0%, rgba(255,255,255,0.08) 40%, rgba(255,255,255,0.25) 100%);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    pointer-events: none;
    z-index: 50;
  }
  
  .screen-content {
    position: relative;
    width: 100%;
    height: 100%;
    background: #0B0F19; /* Authentic AppLock darkTheme background */
    border-radius: 42px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  /* Realistic Status Bar */
  .status-bar {
    height: 46px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 34px;
    position: relative;
    z-index: 40;
    font-size: 15px;
    font-weight: 700;
    color: #F1F5F9;
    background: transparent;
    flex-shrink: 0;
  }
  
  /* Dynamic Island Pill */
  .dynamic-island {
    position: absolute;
    top: 9px;
    left: 50%;
    transform: translateX(-50%);
    width: 136px;
    height: 28px;
    background: #000000;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.6);
  }
  
  .camera-lens {
    width: 11px;
    height: 11px;
    border-radius: 50%;
    background: radial-gradient(circle, #1E293B 40%, #0F172A 90%);
    border: 1px solid #334155;
  }
  
  /* App Body: Takes vertical height between status bar and bottom bar */
  .app-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding: 4px 18px 0;
    justify-content: flex-start;
  }
  
  /* Make the last flex child in app-body stretch to fill remaining space */
  .app-body > div:last-child {
    flex: 1;
    justify-content: flex-start;
  }
  
  /* Authentic Jetpack Compose Bottom Tab Row */
  .compose-tab-bar {
    height: 58px;
    background: #0E1422;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 0 20px;
    position: relative;
    z-index: 40;
    flex-shrink: 0;
  }
  
  .tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    font-weight: 700;
    color: #94A3B8;
  }
  
  .tab-item.active {
    color: #818CF8; /* Authentic AppLock primary */
  }
  
  .tab-item svg {
    width: 24px;
    height: 24px;
  }
  
  /* Gesture Home Bar */
  .home-gesture-bar {
    height: 20px;
    background: #0E1422;
    display: flex;
    align-items: center;
    justify-content: center;
    padding-bottom: 6px;
    flex-shrink: 0;
  }
  
  .home-pill {
    width: 140px;
    height: 5px;
    border-radius: 3px;
    background: rgba(255, 255, 255, 0.65);
  }
</style>
</head>
"""

SCREENS_DATA = [
    # -------------------------------------------------------------
    # IMAGE 1: 0ms INSTANT APP LOCK INTERCEPTION (UnlockActivity)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_1_instant_lock",
        "theme_color_1": "#4F46E5",
        "theme_color_2": "#06B6D4",
        "badge_text": "⚡ 0ms ZERO-DELAY INTERCEPTION",
        "badge_bg": "rgba(79, 70, 229, 0.22)",
        "badge_border": "rgba(56, 189, 248, 0.45)",
        "badge_color": "#38BDF8",
        "title": "Lock Any App With<br><span style='background:linear-gradient(135deg,#38BDF8 0%,#818CF8 50%,#C084FC 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>0ms Instant Interception</span>",
        "subtitle": "Window-level security intercepts launches before a single screen frame appears.",
        "has_tab_bar": False,
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; align-items:center; padding: 6px 16px 0;">
            <!-- Top App Shield Pill -->
            <div style="display:flex; align-items:center; gap: 8px; background: rgba(129,140,248,0.15); border: 1.5px solid rgba(129,140,248,0.35); padding: 8px 24px; border-radius: 999px; font-size: 13.5px; font-weight: 800; color: #818CF8;">
              <svg width="18" height="18" fill="currentColor" viewBox="0 0 24 24"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/></svg>
              APP LOCKER ENTERPRISE SHIELD • 0ms ENGINE
            </div>
            
            <!-- Hero Locked App Graphic -->
            <div style="display:flex; flex-direction:column; align-items:center; margin: 6px 0 4px;">
              <div style="position:relative; width: 124px; height: 124px; border-radius: 34px; background: linear-gradient(135deg, #25D366, #128C7E); display:flex; align-items:center; justify-content:center; box-shadow: 0 20px 45px rgba(37, 211, 102, 0.45); margin-bottom: 14px;">
                <svg width="72" height="72" viewBox="0 0 24 24" fill="white"><path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2.05 22l5.25-1.38c1.45.79 3.08 1.21 4.74 1.21 5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.816 9.816 0 0012.04 2z"/></svg>
                <div style="position:absolute; bottom: -6px; right: -6px; width: 44px; height: 44px; border-radius: 50%; background: #0F172A; border: 2.5px solid #38BDF8; display:flex; align-items:center; justify-content:center; box-shadow: 0 4px 14px rgba(0,0,0,0.6);">
                  <svg width="24" height="24" fill="none" stroke="#38BDF8" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
              </div>
              
              <div style="font-size: 34px; font-weight: 900; color: #FFFFFF; margin-bottom: 2px;">WhatsApp Locked</div>
              <div style="font-size: 17px; font-weight: 600; color: #94A3B8;">Enter master PIN or touch biometric sensor</div>
            </div>
            
            <!-- Security Warning Card -->
            <div style="width: 100%; max-width: 640px; background: rgba(239, 68, 68, 0.14); border: 1.5px solid rgba(239, 68, 68, 0.4); border-radius: 16px; padding: 14px 22px; display:flex; align-items:center; gap: 14px;">
              <span style="font-size: 26px;">⚠️</span>
              <div style="font-size: 15px; color: #FCA5A5; font-weight: 600;">Silent camera armed: 3 attempts remaining before silent camera snapshot.</div>
            </div>
            
            <!-- Glowing PIN Indicators -->
            <div style="display:flex; gap: 30px; margin: 6px 0 10px;">
              <div style="width: 30px; height: 30px; border-radius: 50%; background: #818CF8; box-shadow: 0 0 28px #818CF8;"></div>
              <div style="width: 30px; height: 30px; border-radius: 50%; background: #818CF8; box-shadow: 0 0 28px #818CF8;"></div>
              <div style="width: 30px; height: 30px; border-radius: 50%; background: #818CF8; box-shadow: 0 0 28px #818CF8;"></div>
              <div style="width: 30px; height: 30px; border-radius: 50%; background: rgba(255,255,255,0.12); border: 2.5px solid rgba(255,255,255,0.35);"></div>
            </div>
            
            <!-- Full Numeric Keypad with Sub-Letters (Large 116px buttons) -->
            <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap: 14px 26px; width: 100%; max-width: 640px; margin-bottom: 14px;">
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">1</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">&nbsp;</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">2</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">ABC</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">3</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">DEF</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">4</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">GHI</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">5</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">JKL</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">6</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">MNO</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">7</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">PQRS</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">8</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">TUV</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">9</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">WXYZ</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: rgba(99, 102, 241, 0.18); border: 2px solid #818CF8; display:flex; align-items:center; justify-content:center; color: #818CF8; box-shadow: 0 0 28px rgba(129, 140, 248, 0.38);">
                <svg width="46" height="46" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 004 11m5.918 10.963a14.004 14.004 0 01-5.918-5.963"/></svg>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 42px; font-weight: 800; line-height: 1;">0</span>
                <span style="font-size: 12px; font-weight: 700; color: #64748B; margin-top: 3px;">+</span>
              </div>
              <div style="height: 114px; border-radius: 57px; background: #151D2A; border: 1.5px solid rgba(255,255,255,0.1); display:flex; align-items:center; justify-content:center; color: #94A3B8;">
                <svg width="36" height="36" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.414 6.414a2 2 0 001.414.586H19a2 2 0 002-2V7a2 2 0 00-2-2h-8.172a2 2 0 00-1.414.586L3 12z"/></svg>
              </div>
            </div>
            
            <!-- Android 14 Docked Biometric Bottom Sheet: Deep 360px sheet that docks and fills to the very bottom! -->
            <div style="width: 100%; background: linear-gradient(180deg, #151D2C 0%, #0E1420 100%); border: 1.5px solid rgba(255,255,255,0.14); border-radius: 36px 36px 0 0; padding: 22px 28px 30px; display:flex; flex-direction:column; align-items:center; box-shadow: 0 -18px 45px rgba(0,0,0,0.85);">
              <!-- Handle -->
              <div style="width: 52px; height: 5px; border-radius: 3px; background: rgba(255,255,255,0.3); margin-bottom: 20px;"></div>
              
              <div style="width: 100%; display:flex; align-items:center; justify-content:space-between; margin-bottom: 16px;">
                <div style="display:flex; align-items:center; gap: 16px;">
                  <div style="position:relative; width: 62px; height: 62px; border-radius: 50%; background: rgba(56, 189, 248, 0.16); border: 2px solid #38BDF8; display:flex; align-items:center; justify-content:center; color: #38BDF8; box-shadow: 0 0 24px rgba(56, 189, 248, 0.45);">
                    <svg width="36" height="36" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 004 11m5.918 10.963a14.004 14.004 0 01-5.918-5.963"/></svg>
                    <div style="position:absolute; inset: -6px; border: 1.5px dashed rgba(56, 189, 248, 0.4); border-radius: 50%;"></div>
                  </div>
                  <div>
                    <div style="font-size: 20px; font-weight: 800; color: #FFFFFF;">Biometric Quick Unlock</div>
                    <div style="font-size: 15px; font-weight: 600; color: #10B981; margin-top: 3px;">Touch Fingerprint or Face ID Sensor</div>
                  </div>
                </div>
                <div style="background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); padding: 10px 24px; border-radius: 999px; font-size: 15px; font-weight: 800; color: #FFFFFF;">
                  Use PIN
                </div>
              </div>

              <!-- Extra verification prompt details -->
              <div style="width: 100%; background: rgba(0,0,0,0.35); border: 1px solid rgba(255,255,255,0.06); border-radius: 12px; padding: 10px 16px; display:flex; justify-content:space-between; align-items:center; margin-top: 4px;">
                <span style="font-size: 12.5px; color: #94A3B8; font-weight: 600;">Hardware Keystore Status: Active</span>
                <span style="font-size: 12.5px; color: #34D399; font-weight: 700;">Verified Device Token</span>
              </div>
              
              <div style="font-size: 13.5px; font-weight: 600; color: #64748B; margin-top: 14px;">Forgot Master PIN? Tap for Emergency Keystore Recovery</div>
              
              <!-- Bottom gesture pill -->
              <div style="width: 140px; height: 5px; border-radius: 3px; background: rgba(255,255,255,0.5); margin-top: 22px;"></div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # IMAGE 2: APP HUB (Full Dense List of 19 Apps!)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_2_app_list",
        "theme_color_1": "#2563EB",
        "theme_color_2": "#38BDF8",
        "badge_text": "🛡️ FULL APPLICATION COMMAND HUB",
        "badge_bg": "rgba(37, 99, 235, 0.22)",
        "badge_border": "rgba(56, 189, 248, 0.45)",
        "badge_color": "#60A5FA",
        "title": "One-Tap Protection<br><span style='background:linear-gradient(135deg,#60A5FA 0%,#38BDF8 50%,#818CF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Lock Any App on Device</span>",
        "subtitle": "Protect sensitive messaging, banking, social, and system apps with per-app rules.",
        "has_tab_bar": True,
        "active_tab": "apps",
        "screen_html": """
          <!-- Real App Top Bar -->
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div style="display:flex; align-items:center; gap: 10px;">
              <span style="font-size: 26px; font-weight: 900; color: #FFFFFF; letter-spacing: -0.02em;">App Locker</span>
              <span style="background: linear-gradient(135deg, #F59E0B, #D97706); color: #000000; font-size: 11px; font-weight: 900; padding: 3px 8px; border-radius: 6px; letter-spacing: 0.05em;">PRO</span>
            </div>
            <div style="display:flex; gap: 10px;">
              <div style="width: 36px; height: 36px; border-radius: 50%; background: #182234; display:flex; align-items:center; justify-content:center; color: #94A3B8;">
                <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
              </div>
              <div style="width: 36px; height: 36px; border-radius: 50%; background: #182234; display:flex; align-items:center; justify-content:center; color: #94A3B8;">
                <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              </div>
            </div>
          </div>
          
          <!-- Shield Status Active Card -->
          <div style="background: linear-gradient(90deg, rgba(16, 185, 129, 0.15), rgba(6, 182, 212, 0.1)); border: 1.2px solid rgba(16, 185, 129, 0.35); border-radius: 14px; padding: 9px 16px; margin-bottom: 8px; display:flex; justify-content:space-between; align-items:center;">
            <div style="display:flex; align-items:center; gap: 10px;">
              <div style="width: 10px; height: 10px; border-radius: 50%; background: #10B981; box-shadow: 0 0 10px #10B981;"></div>
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">0ms Shield Active</div>
                <div style="font-size: 11px; font-weight: 600; color: #34D399;">Accessibility Interception Running</div>
              </div>
            </div>
            <div style="font-size: 13px; font-weight: 800; color: #38BDF8;">19 of 32 Locked</div>
          </div>
          
          <!-- Search Bar -->
          <div style="background: #141C2B; border: 1px solid rgba(255,255,255,0.08); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; gap: 10px; margin-bottom: 8px;">
            <svg width="15" height="15" fill="none" stroke="#64748B" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
            <span style="font-size: 13px; color: #64748B; font-weight: 500;">Search 32 system applications...</span>
          </div>
          
          <!-- Filter Tabs -->
          <div style="display:flex; gap: 8px; margin-bottom: 10px;">
            <div style="background: #4F46E5; color: #FFFFFF; font-size: 12px; font-weight: 800; padding: 5px 14px; border-radius: 999px;">All Apps (32)</div>
            <div style="background: #141C2B; color: #94A3B8; font-size: 12px; font-weight: 700; padding: 5px 14px; border-radius: 999px; border: 1px solid rgba(255,255,255,0.06);">Locked (19)</div>
            <div style="background: #141C2B; color: #94A3B8; font-size: 12px; font-weight: 700; padding: 5px 14px; border-radius: 999px; border: 1px solid rgba(255,255,255,0.06);">Unlocked (13)</div>
          </div>
          
          <!-- 19 DENSE APPLICATION ROWS: Fills 100% down to the bottom tab bar! -->
          <div style="display:flex; flex-direction:column; gap: 7px; overflow:hidden;">
            <!-- App 1: WhatsApp -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: linear-gradient(135deg, #25D366, #128C7E); display:flex; align-items:center; justify-content:center; box-shadow: 0 4px 12px rgba(37,211,102,0.3);">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2.05 22l5.25-1.38c1.45.79 3.08 1.21 4.74 1.21 5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.816 9.816 0 0012.04 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">WhatsApp</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 30s delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px; box-shadow: 0 0 10px rgba(79, 70, 229, 0.4);">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 2: Snapchat -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #FFFC00; display:flex; align-items:center; justify-content:center; box-shadow: 0 4px 12px rgba(255,252,0,0.3);">
                  <svg width="22" height="22" fill="#000" viewBox="0 0 24 24"><path d="M12 2c-3.8 0-6.5 2.8-6.5 6.2 0 1.2.4 2.5 1 3.5-.2.5-.9 1.5-2.2 1.9-.3.1-.4.4-.3.7.1.3.4.5.7.5 1.5 0 2.6-.6 3.3-1.1.9.5 2.1.8 3.5.8s2.6-.3 3.5-.8c.7.5 1.8 1.1 3.3 1.1.3 0 .6-.2.7-.5.1-.3 0-.6-.3-.7-1.3-.4-2-1.4-2.2-1.9.6-1 1-2.3 1-3.5 0-3.4-2.7-6.2-6.5-6.2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Snapchat</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #F87171;">Locked • Silent Cam Armed</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 3: Google Photos -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: linear-gradient(135deg, #EA4335, #FBBC05, #34A853, #4285F4); display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M12 2a10 10 0 100 20 10 10 0 000-20zm1 14.5v-4h4v4h-4zm-6-4h4v4H7v-4zm0-6h4v4H7V6.5zm6 0h4v4h-4V6.5z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Google Photos</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 4: Google Wallet -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #1A73E8; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M21 18v1a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v1h-9a2 2 0 00-2 2v8a2 2 0 002 2h9zm-9-2h10V8H12v8zm4-2.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Google Wallet</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #34D399;">Biometric Advisor: 1m delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 5: Instagram -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: linear-gradient(135deg, #833AB4, #FD1D1D, #FCB045); display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Instagram</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 6: Chase Mobile -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #117ACA; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M4 10h16v2H4v-2zm2 4h12v2H6v-2zm-2 4h16v2H4v-2zM12 2L2 7v2h20V7L12 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Chase Mobile</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #38BDF8;">Locked • Banking: 5m delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 7: TikTok -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #000000; border: 1px solid #334155; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M19.59 6.69a4.83 4.83 0 01-3.77-4.25V2h-3.45v13.67a2.89 2.89 0 01-2.88 2.88 2.89 2.89 0 01-2.88-2.88 2.89 2.89 0 012.88-2.88c.41 0 .8.08 1.15.24V9.5a6.32 6.32 0 00-1.15-.11A6.34 6.34 0 003 15.73a6.34 6.34 0 006.34 6.34 6.34 6.34 0 006.34-6.34V8.75a8.16 8.16 0 004.91 1.63V6.93a4.85 4.85 0 01-1-.24z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">TikTok</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 8: Telegram -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #229ED9; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.74-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Telegram</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 30s delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 9: Netflix -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #E50914; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 24px; font-weight: 900; color: white; line-height: 1;">N</span>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Netflix</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 1m delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 10: PayPal -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #003087; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 24px; font-weight: 900; color: #0079C1; line-height: 1;">P</span>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">PayPal</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #34D399;">Locked • Biometric: 2m</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 11: Messenger -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #0084FF; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.03 2 11c0 2.87 1.48 5.43 3.8 7.04V22l3.74-2.05c.78.22 1.6.34 2.46.34 5.52 0 10-4.03 10-9s-4.48-9-10-9zm1.06 12.13l-2.67-2.85-5.21 2.85 5.73-6.08 2.74 2.85 5.14-2.85-5.73 6.08z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Messenger</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 12: Discord -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #5865F2; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M20.317 4.37a19.791 19.791 0 00-4.885-1.515.074.074 0 00-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 00-5.487 0 12.64 12.64 0 00-.617-1.25.077.077 0 00-.079-.037A19.736 19.736 0 003.677 4.37a.07.07 0 00-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 00.031.057 19.9 19.9 0 005.993 3.03.078.078 0 00.084-.028c.462-.63.874-1.295 1.226-1.994.021-.041.001-.09-.041-.106a13.107 13.107 0 01-1.872-.892.077.077 0 01-.008-.128 10.2 10.2 0 00.372-.292.074.074 0 01.077-.01c3.929 1.793 8.18 1.793 12.061 0a.074.074 0 01.078.01c.12.098.246.198.373.292a.077.077 0 01-.006.127 12.299 12.299 0 01-1.873.894.077.077 0 00-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 00.084.028 19.839 19.839 0 006.002-3.03.077.077 0 00.032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 00-.031-.028zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Discord</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 30s delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 13: Settings -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #475569; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 00.12-.61l-1.92-3.32a.488.488 0 00-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 00-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 00-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Settings</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #C084FC;">Anti-Uninstall Protection</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 14: Google Drive -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #0F9D58; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Google Drive</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 15: Spotify -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #1DB954; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="black" viewBox="0 0 24 24"><path d="M12 2C6.477 2 2 6.477 2 12s4.477 10 10 10 10-4.477 10-10S17.523 2 12 2zm4.586 14.424a.625.625 0 01-.86.208c-2.355-1.439-5.32-1.764-8.814-.966a.625.625 0 11-.28-1.22c3.824-.875 7.108-.507 9.746 1.118a.625.625 0 01.208.86zm1.225-2.723a.782.782 0 01-1.077.257c-2.695-1.656-6.804-2.136-9.992-1.168a.782.782 0 01-.462-1.493c3.642-1.106 8.19-.575 11.274 1.327a.782.782 0 01.257 1.077zm.105-2.835C14.692 8.95 9.375 8.775 6.297 9.71a.938.938 0 11-.546-1.794c3.535-1.073 9.404-.866 13.155 1.362a.938.938 0 11-.99 1.588z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Spotify</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 1m delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 16: Zoom -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #2D8CFF; display:flex; align-items:center; justify-content:center;">
                  <svg width="22" height="22" fill="white" viewBox="0 0 24 24"><path d="M4 6a2 2 0 012-2h8a2 2 0 012 2v8a2 2 0 01-2 2H6a2 2 0 01-2-2V6zm14 2.5l4-3v13l-4-3v-7z"/></svg>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Zoom Workplace</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: Immediate</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 17: Slack -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #4A154B; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 22px; font-weight: 900; color: #ECB22E; line-height: 1;">#</span>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Slack</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #94A3B8;">Locked • Relock: 30s delay</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 18: Amazon -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: #FF9900; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 24px; font-weight: 900; color: black; line-height: 1;">a</span>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Amazon Shopping</div>
                  <div style="font-size: 11.5px; font-weight: 700; color: #34D399;">Locked • Biometric: 2m</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <!-- App 19: Google Chrome -->
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 13px; padding: 9px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 12px;">
                <div style="width: 40px; height: 40px; border-radius: 12px; background: linear-gradient(135deg, #EA4335, #4285F4, #FBBC05); display:flex; align-items:center; justify-content:center;">
                  <div style="width: 14px; height: 14px; border-radius: 50%; background: #FFFFFF;"></div>
                </div>
                <div>
                  <div style="font-size: 15px; font-weight: 800; color: #FFFFFF;">Google Chrome</div>
                  <div style="font-size: 11.5px; font-weight: 600; color: #64748B;">Unlocked • Tap to protect</div>
                </div>
              </div>
              <div style="width: 44px; height: 26px; border-radius: 13px; background: #1E293B; display:flex; align-items:center; justify-content:flex-start; padding: 2px;">
                <div style="width: 22px; height: 22px; border-radius: 50%; background: #64748B;"></div>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # IMAGE 3: SILENT INTRUDER SELFIE (Dense Security Audit Logs)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_3_intruder_selfie",
        "theme_color_1": "#DC2626",
        "theme_color_2": "#F97316",
        "badge_text": "📷 SILENT INTRUDER DETECTOR",
        "badge_bg": "rgba(220, 38, 38, 0.22)",
        "badge_border": "rgba(248, 113, 113, 0.45)",
        "badge_color": "#F87171",
        "title": "Catch Intruders<br><span style='background:linear-gradient(135deg,#F87171 0%,#FB923C 50%,#FDE047 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Silent Front-Camera Selfie</span>",
        "subtitle": "Silently captures a high-resolution photo on wrong PIN or Pattern entries.",
        "has_tab_bar": True,
        "active_tab": "logs",
        "screen_html": """
          <!-- Incident Header -->
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div style="display:flex; align-items:center; gap: 8px;">
              <div style="width: 10px; height: 10px; border-radius: 50%; background: #EF4444; box-shadow: 0 0 10px #EF4444;"></div>
              <span style="font-size: 19px; font-weight: 900; color: #EF4444; letter-spacing: 0.04em;">INTRUSION DETECTED</span>
            </div>
            <span style="background: rgba(239, 68, 68, 0.16); color: #FCA5A5; font-size: 11px; font-weight: 800; padding: 4px 10px; border-radius: 6px; border: 1px solid rgba(239, 68, 68, 0.4);">3 Wrong PINs</span>
          </div>
          
          <!-- High-Tech Camera Viewfinder with Crosshairs & EXIF (420px tall) -->
          <div style="position:relative; width: 100%; height: 420px; border-radius: 24px; background: #060911; border: 1.5px solid rgba(239, 68, 68, 0.5); overflow:hidden; display:flex; flex-direction:column; justify-content:space-between; padding: 16px; margin-bottom: 8px; box-shadow: 0 16px 40px rgba(0,0,0,0.8), inset 0 0 45px rgba(239,68,68,0.15);">
            <!-- Top HUD -->
            <div style="display:flex; justify-content:space-between; align-items:center; z-index: 10;">
              <div style="display:flex; align-items:center; gap: 8px; font-size: 11px; font-weight: 800; color: #EF4444; letter-spacing: 0.08em;">
                <div style="width: 8px; height: 8px; border-radius: 50%; background: #EF4444; animation: pulse 1s infinite;"></div>
                REC • SILENT CAMERA BUFFER
              </div>
              <div style="font-size: 11px; font-weight: 700; color: #94A3B8;">EXIF: 1080p UHD • F/1.8</div>
            </div>
            
            <!-- Viewfinder Brackets -->
            <div style="position:absolute; top: 16px; left: 16px; width: 34px; height: 34px; border-top: 3px solid #EF4444; border-left: 3px solid #EF4444; border-top-left-radius: 8px;"></div>
            <div style="position:absolute; top: 16px; right: 16px; width: 34px; height: 34px; border-top: 3px solid #EF4444; border-right: 3px solid #EF4444; border-top-right-radius: 8px;"></div>
            <div style="position:absolute; bottom: 16px; left: 16px; width: 34px; height: 34px; border-bottom: 3px solid #EF4444; border-left: 3px solid #EF4444; border-bottom-left-radius: 8px;"></div>
            <div style="position:absolute; bottom: 16px; right: 16px; width: 34px; height: 34px; border-bottom: 3px solid #EF4444; border-right: 3px solid #EF4444; border-bottom-right-radius: 8px;"></div>
            
            <!-- Center Crosshair & Face Target -->
            <div style="position:absolute; inset: 0; display:flex; flex-direction:column; align-items:center; justify-content:center;">
              <div style="position:relative; width: 156px; height: 156px; border-radius: 50%; background: radial-gradient(circle, #334155 0%, #1E293B 70%, #0F172A 100%); border: 3px solid #EF4444; display:flex; align-items:center; justify-content:center; box-shadow: 0 0 45px rgba(239, 68, 68, 0.4);">
                <svg width="88" height="88" fill="#64748B" viewBox="0 0 24 24"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                <div style="position:absolute; inset: -14px; border: 1.5px dashed rgba(239, 68, 68, 0.6); border-radius: 50%;"></div>
              </div>
              <div style="margin-top: 16px; background: linear-gradient(135deg, #DC2626, #EF4444); color: white; font-size: 12.5px; font-weight: 900; padding: 5px 18px; border-radius: 999px; letter-spacing: 0.06em; box-shadow: 0 6px 20px rgba(220, 38, 38, 0.5);">
                FACE MATCH • 99.4% CONFIDENCE
              </div>
            </div>
            
            <!-- Bottom HUD -->
            <div style="display:flex; justify-content:space-between; align-items:center; z-index: 10;">
              <div style="font-size: 11px; font-weight: 700; color: #FCA5A5;">GPS: 37.7749° N, 122.4194° W</div>
              <div style="font-size: 11px; font-weight: 800; color: #EF4444;">SNAPCHAT • 14:28:05.184</div>
            </div>
          </div>
          
          <!-- Incident Metadata Strip -->
          <div style="background: #121927; border: 1px solid rgba(255,255,255,0.08); border-radius: 12px; padding: 9px 16px; margin-bottom: 8px;">
            <div style="display:flex; justify-content:space-between; font-size: 12px; margin-bottom: 4px;">
              <span style="color: #94A3B8; font-weight: 600;">Target Application</span>
              <span style="color: #FFFFFF; font-weight: 800;">Snapchat & Private Gallery</span>
            </div>
            <div style="display:flex; justify-content:space-between; font-size: 12px;">
              <span style="color: #94A3B8; font-weight: 600;">Storage Protection</span>
              <span style="color: #34D399; font-weight: 800;">AES-256 GCM KeyStore Sandbox</span>
            </div>
          </div>
          
          <!-- Evidence Action Buttons -->
          <div style="display:flex; gap: 10px; margin-bottom: 10px;">
            <div style="flex:1; background: #EF4444; color: #FFFFFF; font-size: 13px; font-weight: 800; padding: 9px; border-radius: 10px; text-align:center; box-shadow: 0 6px 18px rgba(239, 68, 68, 0.4);">
              Inspect Evidence
            </div>
            <div style="flex:1; background: #1E293B; border: 1px solid rgba(255,255,255,0.12); color: #E2E8F0; font-size: 13px; font-weight: 800; padding: 9px; border-radius: 10px; text-align:center;">
              Delete & Shred
            </div>
          </div>
          
          <!-- Section Title -->
          <div style="font-size: 11.5px; font-weight: 800; color: #64748B; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 6px;">
            PRIOR INTRUDER INCIDENT RECORDS (13)
          </div>
          
          <!-- 13 INCIDENT LOG ROWS: Fills all the way down to bottom navigation tab bar! -->
          <div style="display:flex; flex-direction:column; gap: 7px; overflow:hidden;">
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Instagram Intrusion Attempt</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Today, 11:15 AM • 2 Wrong PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Google Photos Intrusion</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Yesterday, 19:40 PM • 1 Wrong Pattern</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">WhatsApp Secret Chat Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Yesterday, 14:12 PM • 3 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Telegram Vault Access Trigger</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">2 days ago, 22:04 PM • 2 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(99, 102, 241, 0.18); display:flex; align-items:center; justify-content:center; color: #818CF8;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Settings Anti-Uninstall Trigger</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">3 days ago, 08:30 AM • Tamper Blocked</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #F59E0B; background: rgba(245, 158, 11, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Snapchat Stealth Sniffer</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">3 days ago, 16:10 PM • Unauthorized Pattern</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Facebook Messenger Breach</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">4 days ago, 19:02 PM • Face ID Mismatch</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(99, 102, 241, 0.18); display:flex; align-items:center; justify-content:center; color: #818CF8;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Binance Cold Wallet Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">5 days ago, 03:14 AM • Keyguard Tamper</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #F59E0B; background: rgba(245, 158, 11, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Twitter / X DM Vault Intercept</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">6 days ago, 14:10 PM • 3 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Outlook Work Email Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">7 days ago, 09:15 AM • Unauthorized PIN</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(239, 68, 68, 0.16); display:flex; align-items:center; justify-content:center; color: #EF4444;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Amazon Shopping Breach Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">8 days ago, 20:30 PM • 2 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #10B981; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Captured ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(99, 102, 241, 0.18); display:flex; align-items:center; justify-content:center; color: #818CF8;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Crypto Wallet Recovery Attempt</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">9 days ago, 02:44 AM • Tamper Blocked</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #F59E0B; background: rgba(245, 158, 11, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(99, 102, 241, 0.18); display:flex; align-items:center; justify-content:center; color: #818CF8;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">System Boot Intercept</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">10 days ago, 18:20 PM • Hardware Blocked</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #F59E0B; background: rgba(245, 158, 11, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # IMAGE 4: BIOMETRIC VAULT WITH TAP-TO-REVEAL (Fully Dense)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_4_biometric_vault",
        "theme_color_1": "#059669",
        "theme_color_2": "#10B981",
        "badge_text": "👁️ TAP-TO-REVEAL BIOMETRICS",
        "badge_bg": "rgba(5, 150, 105, 0.22)",
        "badge_border": "rgba(52, 211, 153, 0.45)",
        "badge_color": "#34D399",
        "title": "Intruder Evidence With<br><span style='background:linear-gradient(135deg,#34D399 0%,#22D3EE 50%,#818CF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Tap-To-Reveal Shield</span>",
        "subtitle": "Sensitive photos stay blurred on disk until unlocked with Fingerprint or Face ID.",
        "has_tab_bar": True,
        "active_tab": "logs",
        "screen_html": """
          <!-- Header -->
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div>
              <div style="font-size: 24px; font-weight: 900; color: #FFFFFF;">Intruder Vault</div>
              <div style="font-size: 12px; font-weight: 600; color: #94A3B8;">Hardware KeyStore Protected Subsystem</div>
            </div>
            <div style="background: rgba(16, 185, 129, 0.15); border: 1.2px solid rgba(16, 185, 129, 0.4); padding: 4px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 800; color: #34D399;">
              13 Encrypted Photos
            </div>
          </div>
          
          <!-- KeyStore Status Bar -->
          <div style="background: #121927; border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 12px; padding: 8px 14px; display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div style="display:flex; align-items:center; gap: 8px;">
              <div style="width: 8px; height: 8px; border-radius: 50%; background: #10B981; box-shadow: 0 0 8px #10B981;"></div>
              <span style="font-size: 12px; font-weight: 700; color: #E2E8F0;">AES-256 GCM Hardware Sandbox • 0 Cloud Uploads</span>
            </div>
            <span style="font-size: 11px; font-weight: 800; color: #34D399;">100% OFFLINE</span>
          </div>
          
          <!-- Frosted Glass Tap-to-Reveal Card (380px tall) -->
          <div style="position:relative; width: 100%; height: 380px; border-radius: 22px; background: linear-gradient(145deg, rgba(16, 185, 129, 0.08), rgba(6, 78, 59, 0.25)); border: 1.5px solid rgba(52, 211, 153, 0.35); overflow:hidden; display:flex; flex-direction:column; align-items:center; justify-content:center; padding: 20px; margin-bottom: 8px; box-shadow: 0 16px 40px rgba(0,0,0,0.7);">
            <!-- Background Frosted Silhouette -->
            <div style="position:absolute; width: 200px; height: 200px; border-radius: 50%; background: #334155; filter: blur(35px); opacity: 0.4;"></div>
            
            <!-- Glowing Fingerprint Scanner Pulse -->
            <div style="position:relative; width: 116px; height: 116px; border-radius: 50%; background: rgba(16, 185, 129, 0.2); border: 2.5px solid #10B981; display:flex; align-items:center; justify-content:center; color: #10B981; box-shadow: 0 0 50px rgba(16, 185, 129, 0.45); margin-bottom: 18px;">
              <svg width="64" height="64" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 004 11m5.918 10.963a14.004 14.004 0 01-5.918-5.963"/></svg>
              <div style="position:absolute; inset: -10px; border: 1.5px solid rgba(52, 211, 153, 0.4); border-radius: 50%;"></div>
            </div>
            
            <div style="font-size: 26px; font-weight: 900; color: #FFFFFF; margin-bottom: 4px; z-index: 10;">Tap to Reveal Photo</div>
            <div style="font-size: 14px; font-weight: 600; color: #34D399; margin-bottom: 14px; z-index: 10;">Fingerprint or Face ID Verification</div>
            
            <div style="background: rgba(0, 0, 0, 0.6); border: 1.2px solid rgba(52, 211, 153, 0.5); padding: 6px 18px; border-radius: 999px; font-size: 11.5px; font-weight: 800; color: #A7F3D0; margin-bottom: 16px; z-index: 10;">
              HARDWARE KEYSTORE CERTIFIED ✓
            </div>
            
            <div style="font-size: 11px; font-weight: 700; color: #64748B; letter-spacing: 0.05em; z-index: 10; margin-bottom: 4px;">AES-256-GCM KEY: AppLock_Master_Key • IV: 0x7F4B2E910A</div>
            <div style="font-size: 10px; font-weight: 700; color: #475569; letter-spacing: 0.08em; text-transform:uppercase; z-index: 10;">ENCRYPTED AT REST VIA ANDROID KEYSTORE ENCLAVE</div>
          </div>
          
          <!-- Section Title -->
          <div style="font-size: 11.5px; font-weight: 800; color: #64748B; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 6px;">
            VAULT INCIDENT RECORDS (13)
          </div>
          
          <!-- 13 VAULT RECORDS: Fills all the way down to bottom navigation tab bar! -->
          <div style="display:flex; flex-direction:column; gap: 7px; overflow:hidden;">
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Snapchat Unauthorized Attempt</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Today, 14:28 PM • 3 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Instagram Attempt Blocked</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Today, 11:15 AM • 2 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Private Photos Access Denied</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Yesterday, 19:40 PM • 1 Wrong Pattern</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">WhatsApp Secret Chat Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Yesterday, 14:12 PM • 3 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Chase Mobile Unauthorized Access</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">2 days ago, 21:15 PM • 2 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Telegram Hidden Vault Trigger</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">3 days ago, 16:30 PM • 3 Failed PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Signal Encrypted Backup Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">4 days ago, 11:05 AM • Biometric Fail</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Google Drive Folder Vault Denied</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">5 days ago, 08:22 AM • Unauthorized PIN</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Chrome Incognito Session Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">6 days ago, 23:19 PM • Silent Cam Snapped</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Outlook Secure Mail Sniffer</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">7 days ago, 11:40 AM • 2 Wrong PINs</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Banking Statement PDF Vault</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">8 days ago, 14:12 PM • Biometric Fail</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Twitter Direct Messages Probe</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">9 days ago, 08:05 AM • Tamper Blocked</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 8px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 32px; height: 32px; border-radius: 8px; background: rgba(16, 185, 129, 0.16); display:flex; align-items:center; justify-content:center; color: #34D399;">
                  <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">System Settings Uninstall Intercept</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">10 days ago, 17:40 PM • Tamper Shielded</div>
                </div>
              </div>
              <span style="font-size: 11px; font-weight: 800; color: #34D399; background: rgba(16, 185, 129, 0.12); padding: 3px 8px; border-radius: 6px;">Shielded 🔒</span>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # IMAGE 5: MILITARY-GRADE SECURITY CENTER (Rich Categorized Controls)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_5_security_center",
        "theme_color_1": "#6366F1",
        "theme_color_2": "#8B5CF6",
        "badge_text": "🛡️ HARDWARE-BACKED SECURITY",
        "badge_bg": "rgba(99, 102, 241, 0.22)",
        "badge_border": "rgba(129, 140, 248, 0.45)",
        "badge_color": "#818CF8",
        "title": "Military-Grade<br><span style='background:linear-gradient(135deg,#818CF8 0%,#C084FC 50%,#38BDF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>AES-256 Hardware Enclave</span>",
        "subtitle": "Encryption keys generated and stored strictly inside the Android KeyStore hardware.",
        "has_tab_bar": True,
        "active_tab": "security",
        "screen_html": """
          <!-- Security Center Header -->
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div>
              <div style="font-size: 24px; font-weight: 900; color: #FFFFFF;">Security Center</div>
              <div style="font-size: 12px; font-weight: 600; color: #94A3B8;">Hardware Cryptographic Subsystem Active</div>
            </div>
            <div style="background: rgba(99, 102, 241, 0.18); border: 1.2px solid rgba(99, 102, 241, 0.4); padding: 4px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 800; color: #A5B4FC;">
              TEE Enclave
            </div>
          </div>
          
          <!-- Titan TEE / KeyStore Hardware Hero Card (320px tall) -->
          <div style="background: linear-gradient(145deg, #181A2E, #0E1222); border: 1.5px solid rgba(129, 140, 248, 0.35); border-radius: 20px; padding: 16px; margin-bottom: 8px; display:flex; flex-direction:column; align-items:center; text-align:center; box-shadow: 0 12px 35px rgba(0,0,0,0.6);">
            <div style="width: 62px; height: 62px; border-radius: 18px; background: rgba(99, 102, 241, 0.2); border: 2px solid #818CF8; display:flex; align-items:center; justify-content:center; color: #818CF8; margin-bottom: 10px; box-shadow: 0 0 25px rgba(129, 140, 248, 0.4);">
              <svg width="34" height="34" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"/></svg>
            </div>
            <div style="font-size: 19px; font-weight: 900; color: #FFFFFF; letter-spacing: 0.04em;">TITAN TEE / KEYSTORE</div>
            <div style="font-size: 12.5px; font-weight: 700; color: #818CF8; margin-top: 2px;">AES-256-GCM Hardware Cipher</div>
            <div style="background: rgba(0,0,0,0.5); border: 1px solid rgba(255,255,255,0.1); padding: 5px 14px; border-radius: 8px; font-size: 11px; font-weight: 700; color: #94A3B8; margin-top: 8px;">
              KEYSTORE ALIAS: AppLocker_GCM_Key [FIPS 140-2 LEVEL 3]
            </div>
            <div style="font-size: 11px; font-weight: 600; color: #64748B; margin-top: 6px;">Key Derivation: PBKDF2 • 100,000 Iterations • Hardware Keyset</div>
          </div>
          
          <!-- Category 1: Active Configuration -->
          <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 9px 14px; margin-bottom: 6px; display:flex; justify-content:space-between; align-items:center;">
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Active Lock Mode</div>
              <div style="font-size: 11.5px; font-weight: 600; color: #818CF8;">4-Digit Numeric PIN (Active)</div>
            </div>
            <div style="background: #252D3F; border: 1px solid rgba(255,255,255,0.1); padding: 5px 12px; border-radius: 8px; font-size: 12px; font-weight: 800; color: #C7D2FE;">
              Change PIN
            </div>
          </div>
          
          <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 12px; padding: 9px 14px; margin-bottom: 8px; display:flex; justify-content:space-between; align-items:center;">
            <div>
              <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Global Re-Lock Delay</div>
              <div style="font-size: 11.5px; font-weight: 600; color: #34D399;">30 Seconds (Smart Optimized)</div>
            </div>
            <div style="background: #252D3F; border: 1px solid rgba(255,255,255,0.1); padding: 5px 12px; border-radius: 8px; font-size: 12px; font-weight: 800; color: #6EE7B7;">
              Configure
            </div>
          </div>
          
          <!-- Category 2: Core Protection Toggles -->
          <div style="font-size: 11px; font-weight: 800; color: #64748B; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 5px;">
            CORE PROTECTION & STEALTH CONTROLS (14)
          </div>
          
          <!-- 14 MATERIAL TOGGLES: Fills all the way down to bottom navigation tab bar! -->
          <div style="display:flex; flex-direction:column; gap: 6px; overflow:hidden;">
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">0ms Window Interception</div>
                <div style="font-size: 11px; font-weight: 600; color: #34D399;">Accessibility Service Active</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Silent Intruder Selfie</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Capture after 2 failed PINs</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Biometric Unlock</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Fingerprint & Face ID Supported</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Anti-Uninstall Protection</div>
                <div style="font-size: 11px; font-weight: 700; color: #C084FC;">Blocks Settings & Play Store</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">3-Pass Forensic Shredder</div>
                <div style="font-size: 11px; font-weight: 700; color: #34D399;">DoD 5220.22-M Overwrite: ON</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Auto-Purge Old Logs</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">GDPR Compliant 30-Day Expiry</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">App Disguise Mode</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Camouflage Icon as Calculator</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Anti-Screen Recording Shield</div>
                <div style="font-size: 11px; font-weight: 600; color: #38BDF8;">Hardware FLAG_SECURE Active</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Random Keypad Scramble</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Shuffles PIN digits against shoulder surfing</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Root & Magisk Cloaking Shield</div>
                <div style="font-size: 11px; font-weight: 700; color: #34D399;">Play Integrity Attestation: PASS</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Panic Trigger Gesture</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Shake device to immediately lock all apps</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Failed Attempt Lockout</div>
                <div style="font-size: 11px; font-weight: 600; color: #F87171;">Lock device for 5 mins after 5 wrong attempts</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Notification Content Scrambler</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Hides preview text from locked apps</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 14px; display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size: 13.5px; font-weight: 800; color: #FFFFFF;">Fake Crash Decoy Dialog</div>
                <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Displays 'App Has Stopped' decoy error</div>
              </div>
              <div style="width: 40px; height: 24px; border-radius: 12px; background: #4F46E5; display:flex; align-items:center; justify-content:flex-end; padding: 2px;">
                <div style="width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;"></div>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # IMAGE 6: ZERO-LOOP DOUBLE LOCK ADVISOR (Dense Banking List)
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_6_double_lock_advisor",
        "theme_color_1": "#0D9488",
        "theme_color_2": "#10B981",
        "badge_text": "🔄 ZERO-LOOP SMART ADVISOR",
        "badge_bg": "rgba(13, 148, 136, 0.22)",
        "badge_border": "rgba(45, 212, 191, 0.45)",
        "badge_color": "#2DD4BF",
        "title": "Smart Advisor<br><span style='background:linear-gradient(135deg,#2DD4BF 0%,#34D399 50%,#38BDF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Zero-Loop Protection</span>",
        "subtitle": "Intelligently prevents annoying double-authentication loops for banking & biometric apps.",
        "has_tab_bar": True,
        "active_tab": "security",
        "screen_html": """
          <!-- Advisor Header -->
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 8px;">
            <div>
              <div style="font-size: 24px; font-weight: 900; color: #FFFFFF;">Double-Lock Advisor</div>
              <div style="font-size: 12px; font-weight: 600; color: #94A3B8;">Smart Conflict Prevention Engine</div>
            </div>
            <div style="background: rgba(45, 212, 191, 0.15); border: 1.2px solid rgba(45, 212, 191, 0.4); padding: 4px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 800; color: #2DD4BF;">
              Auto-Optimized
            </div>
          </div>
          
          <!-- Conflict Banner -->
          <div style="background: linear-gradient(135deg, rgba(13, 148, 136, 0.2), rgba(6, 95, 70, 0.25)); border: 1.5px solid rgba(45, 212, 191, 0.45); border-radius: 14px; padding: 12px 16px; margin-bottom: 8px;">
            <div style="display:flex; align-items:center; gap: 8px; margin-bottom: 5px;">
              <span style="font-size: 17px;">💡</span>
              <span style="font-size: 14px; font-weight: 800; color: #5EEAD4;">Conflict Resolved Automatically</span>
            </div>
            <div style="font-size: 12px; color: #CCFBF1; line-height: 1.35; font-weight: 500;">
              WhatsApp, Google Wallet, and Chase Banking have internal biometric security active. App Locker auto-applied optimal relock timing to eliminate annoying double authentication prompts.
            </div>
          </div>
          
          <!-- Relock Presets -->
          <div style="font-size: 11px; font-weight: 800; color: #64748B; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 5px;">
            GLOBAL RELOCK TIMEOUT PRESETS
          </div>
          <div style="display:flex; gap: 7px; margin-bottom: 9px;">
            <div style="background: #141C2B; border: 1px solid rgba(255,255,255,0.08); padding: 5px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 700; color: #94A3B8;">Immediate</div>
            <div style="background: #0D9488; border: 1px solid #2DD4BF; padding: 5px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 800; color: #FFFFFF;">30s Delay ✓</div>
            <div style="background: #141C2B; border: 1px solid rgba(255,255,255,0.08); padding: 5px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 700; color: #94A3B8;">1 min</div>
            <div style="background: #141C2B; border: 1px solid rgba(255,255,255,0.08); padding: 5px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 700; color: #94A3B8;">2 min</div>
            <div style="background: #141C2B; border: 1px solid rgba(255,255,255,0.08); padding: 5px 12px; border-radius: 999px; font-size: 11.5px; font-weight: 700; color: #94A3B8;">5 min</div>
          </div>
          
          <!-- List Title -->
          <div style="font-size: 11px; font-weight: 800; color: #64748B; letter-spacing: 0.08em; text-transform: uppercase; margin-bottom: 5px;">
            ACTIVE APP CONFLICT RULES (17)
          </div>
          
          <!-- 17 DENSE CONFLICT RESOLUTION ROWS: Fills all the way down to bottom navigation tab bar! -->
          <div style="display:flex; flex-direction:column; gap: 6px; overflow:hidden;">
            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #25D366; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2.05 22l5.25-1.38c1.45.79 3.08 1.21 4.74 1.21 5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.816 9.816 0 0012.04 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">WhatsApp</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Native Fingerprint Detected</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #2DD4BF; background: rgba(13, 148, 136, 0.2); border: 1px solid rgba(45, 212, 191, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 30s ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #1A73E8; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M21 18v1a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h14a2 2 0 012 2v1h-9a2 2 0 00-2 2v8a2 2 0 002 2h9zm-9-2h10V8H12v8zm4-2.5a1.5 1.5 0 110-3 1.5 1.5 0 010 3z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Google Wallet</div>
                  <div style="font-size: 11px; font-weight: 700; color: #38BDF8;">System Biometrics Active</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 1m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #117ACA; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M4 10h16v2H4v-2zm2 4h12v2H6v-2zm-2 4h16v2H4v-2zM12 2L2 7v2h20V7L12 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Chase Mobile Banking</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Session Timeout Synced</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 5m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #003087; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: #0079C1;">P</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">PayPal Mobile</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Biometric Fast-Login</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 2m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #000000; border: 1px solid #334155; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: white;">R</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Revolut Banking</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Internal Face ID Active</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 5m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #E31837; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M4 10h16v2H4v-2zm2 4h12v2H6v-2zm-2 4h16v2H4v-2zM12 2L2 7v2h20V7L12 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Bank of America</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Biometric Session Active</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 5m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #00C805; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Robinhood Investing</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Native Biometrics Active</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 2m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #F0B90B; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: black;">B</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Binance Crypto</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">2FA Biometric Passkey</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 3m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #0052FF; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: white;">C</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Coinbase Wallet</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Biometric Enclave Synced</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 5m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #D71E28; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M4 10h16v2H4v-2zm2 4h12v2H6v-2zm-2 4h16v2H4v-2zM12 2L2 7v2h20V7L12 2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Wells Fargo Mobile</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Native Touch ID Active</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 5m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #008CFF; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: white;">V</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Venmo Payments</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Instant Lock Suppressed</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 2m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #00D632; display:flex; align-items:center; justify-content:center;">
                  <span style="font-size: 18px; font-weight: 900; color: white;">$</span>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Cash App</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">PIN Lock Aligned</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 2m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #FFFC00; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="#000" viewBox="0 0 24 24"><path d="M12 2c-3.8 0-6.5 2.8-6.5 6.2 0 1.2.4 2.5 1 3.5-.2.5-.9 1.5-2.2 1.9-.3.1-.4.4-.3.7.1.3.4.5.7.5 1.5 0 2.6-.6 3.3-1.1.9.5 2.1.8 3.5.8s2.6-.3 3.5-.8c.7.5 1.8 1.1 3.3 1.1.3 0 .6-.2.7-.5.1-.3 0-.6-.3-.7-1.3-.4-2-1.4-2.2-1.9.6-1 1-2.3 1-3.5 0-3.4-2.7-6.2-6.5-6.2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Snapchat</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Standard Instant Protection</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 700; color: #E2E8F0; background: rgba(255,255,255,0.08); padding: 3px 10px; border-radius: 6px;">Immediate</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: linear-gradient(135deg, #833AB4, #FD1D1D, #FCB045); display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838a6.162 6.162 0 100 12.324 6.162 6.162 0 000-12.324z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Instagram</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Standard Instant Protection</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 700; color: #E2E8F0; background: rgba(255,255,255,0.08); padding: 3px 10px; border-radius: 6px;">Immediate</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: linear-gradient(135deg, #EA4335, #FBBC05, #34A853, #4285F4); display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M12 2a10 10 0 100 20 10 10 0 000-20zm1 14.5v-4h4v4h-4zm-6-4h4v4H7v-4zm0-6h4v4H7V6.5zm6 0h4v4h-4V6.5z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Google Photos</div>
                  <div style="font-size: 11px; font-weight: 600; color: #94A3B8;">Standard Instant Protection</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 700; color: #E2E8F0; background: rgba(255,255,255,0.08); padding: 3px 10px; border-radius: 6px;">Immediate</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #229ED9; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.74-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Telegram Private Vault</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Passcode Pass-Through</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 1m ✓</span>
            </div>

            <div style="background: #121927; border: 1px solid rgba(255,255,255,0.07); border-radius: 11px; padding: 7px 12px; display:flex; align-items:center; justify-content:space-between;">
              <div style="display:flex; align-items:center; gap: 10px;">
                <div style="width: 34px; height: 34px; border-radius: 10px; background: #3A76F0; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="white" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/></svg>
                </div>
                <div>
                  <div style="font-size: 14px; font-weight: 800; color: #FFFFFF;">Signal Messenger</div>
                  <div style="font-size: 11px; font-weight: 700; color: #34D399;">Screen Lock Integrated</div>
                </div>
              </div>
              <span style="font-size: 11.5px; font-weight: 800; color: #38BDF8; background: rgba(56, 189, 248, 0.2); border: 1px solid rgba(56, 189, 248, 0.4); padding: 3px 10px; border-radius: 6px;">Delay: 30s ✓</span>
            </div>
          </div>
        """
    }
]

def generate_screen_html(data):
    # Compose tab bar HTML if applicable
    tab_bar_html = ""
    if data.get("has_tab_bar", True):
        active_tab = data.get("active_tab", "apps")
        tab_bar_html = f"""
        <div class="compose-tab-bar">
          <div class="tab-item {'active' if active_tab == 'apps' else ''}">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4v4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z"/></svg>
            <span>Apps</span>
          </div>
          <div class="tab-item {'active' if active_tab == 'security' else ''}">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
            <span>Security</span>
          </div>
          <div class="tab-item {'active' if active_tab == 'logs' else ''}">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4zm7-9h-3.17l-1.83-2H10L8.17 5H5c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2z"/></svg>
            <span>Logs</span>
          </div>
        </div>
        <div class="home-gesture-bar">
          <div class="home-pill"></div>
        </div>
        """

    return f"""<!DOCTYPE html>
<html lang="en">
{COMMON_CSS}
<body>
  <div class="canvas">
    <div class="grid-pattern"></div>
    <div class="noise-overlay"></div>
    <div class="glow-top" style="background: radial-gradient(circle, {data['theme_color_1']} 0%, {data['theme_color_2']} 50%, transparent 80%);"></div>
    <div class="glow-bottom" style="background: radial-gradient(circle, {data['theme_color_1']} 0%, {data['theme_color_2']} 50%, transparent 80%);"></div>

    <!-- Category Pill -->
    <div class="category-pill" style="background: {data['badge_bg']}; border: 1.5px solid {data['badge_border']}; color: {data['badge_color']};">
      {data['badge_text']}
    </div>

    <!-- Hero Title -->
    <h1 class="hero-title" style="color: #FFFFFF;">
      {data['title']}
    </h1>

    <!-- Hero Subtitle -->
    <p class="hero-subtitle">
      {data['subtitle']}
    </p>

    <!-- Phone Stage -->
    <div class="device-stage">
      <div class="phone-frame">
        <div class="screen-content">
          <!-- Status Bar -->
          <div class="status-bar">
            <span>9:41</span>
            <div class="dynamic-island">
              <div class="camera-lens"></div>
            </div>
            <div style="display:flex; align-items:center; gap: 8px;">
              <svg width="18" height="13" viewBox="0 0 15 11" fill="#F1F5F9"><path d="M0 8.5h2v2H0v-2zm3.5-3h2v5h-2v-5zm3.5-3h2v8h-2v-8zm3.5-2.5h2v10.5h-2V0z"/></svg>
              <span style="font-size:13px; font-weight:800;">5G</span>
              <svg width="24" height="12" viewBox="0 0 22 11" fill="none" stroke="#F1F5F9"><rect x="0.5" y="0.5" width="18" height="10" rx="3"/><path d="M20 3.5v4" stroke-width="2"/><rect x="2" y="2" width="15" height="7" rx="1.5" fill="#10B981"/></svg>
            </div>
          </div>
          
          <!-- App Body -->
          <div class="app-body">
            {data['screen_html']}
          </div>

          <!-- Compose Bottom Tab Bar -->
          {tab_bar_html}
        </div>
      </div>
    </div>
  </div>
</body>
</html>
"""

def main():
    temp_dir = Path("D:/app-locker/store_assets/.temp_real_html")
    temp_dir.mkdir(parents=True, exist_ok=True)
    
    # Clean old screenshots in directory
    for old_file in OUTPUT_DIR.glob("*.png"):
        old_file.unlink()
        
    print("Generating 6 100% Fully-Packed Real App Interface Images for Google Play Store...")
    
    for idx, screen in enumerate(SCREENS_DATA, start=1):
        html_content = generate_screen_html(screen)
        html_file = temp_dir / f"{screen['id']}.html"
        raw_png = temp_dir / f"{screen['id']}_raw.png"
        final_png = OUTPUT_DIR / f"{screen['id']}.png"
        
        html_file.write_text(html_content, encoding="utf-8")
        file_url = f"file:///{html_file.resolve().as_posix()}"
        print(f"[{idx}/6] Rendering {final_png.name} via Edge Headless...")
        
        cmd = [
            EDGE_PATH,
            "--headless",
            f"--screenshot={raw_png.resolve()}",
            "--window-size=1080,1920",
            "--hide-scrollbars",
            "--force-device-scale-factor=1",
            "--disable-gpu",
            file_url
        ]
        
        subprocess.run(cmd, check=True)
        
        # Verify & convert strictly to 24-bit RGB PNG (no alpha channel)
        with Image.open(raw_png) as img:
            img = img.crop((0, 0, 1080, 1920))
            rgb_img = img.convert("RGB")
            rgb_img.save(final_png, "PNG", optimize=True)
            
        file_size_kb = final_png.stat().st_size / 1024
        print(f"      [OK] Generated: {final_png.name} ({file_size_kb:.1f} KB, RGB 24-bit, 1080x1920)")
        
    # Cleanup temp html files
    for tmp in temp_dir.glob("*"):
        tmp.unlink()
    temp_dir.rmdir()
    
    print("\nAll 6 100% Fully-Packed Play Store screenshots created successfully!")

if __name__ == "__main__":
    main()
