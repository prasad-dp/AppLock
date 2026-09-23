"""
Studio-Grade Showcase Graphic Generator for Google Play Store / iOS App Store
Designed for App Locker & Intruder Vault
Outputs 8 ultra-premium, high-density 1080x1920 24-bit RGB PNG screenshots
"""

import os
import subprocess
from pathlib import Path
from PIL import Image

EDGE_PATH = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
OUTPUT_DIR = Path("D:/app-locker/store_assets/screenshots")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

COMMON_HEAD = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<style>
  @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800;900&display=swap');
  
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
    -webkit-font-smoothing: antialiased;
  }
  
  body {
    width: 1080px;
    height: 1920px;
    overflow: hidden;
    background: #06080E;
    font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
    color: #FFFFFF;
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    padding: 85px 44px 0;
  }
  
  /* Cyber Grid Background */
  .cyber-grid {
    position: absolute;
    inset: 0;
    background-image: 
      linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
    background-size: 60px 60px;
    opacity: 0.6;
    z-index: 1;
    pointer-events: none;
  }
  
  /* Ambient Cosmic Glows */
  .glow-orb {
    position: absolute;
    border-radius: 50%;
    filter: blur(120px);
    pointer-events: none;
    z-index: 0;
  }
  
  /* Top Pill Badge */
  .pill-badge {
    position: relative;
    z-index: 10;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 11px 26px;
    border-radius: 999px;
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 0.12em;
    text-transform: uppercase;
    backdrop-filter: blur(20px);
    margin-bottom: 22px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
  }
  
  /* Hero Typography */
  .hero-title {
    position: relative;
    z-index: 10;
    font-size: 62px;
    font-weight: 900;
    text-align: center;
    line-height: 1.14;
    letter-spacing: -0.035em;
    max-width: 980px;
    margin-bottom: 16px;
    text-shadow: 0 6px 24px rgba(0, 0, 0, 0.7);
  }
  
  .hero-subtitle {
    position: relative;
    z-index: 10;
    font-size: 26px;
    font-weight: 500;
    color: #94A3B8;
    text-align: center;
    max-width: 860px;
    line-height: 1.4;
    letter-spacing: -0.01em;
    margin-bottom: 45px;
  }
  
  /* Phone Frame Container */
  .device-stage {
    position: relative;
    z-index: 10;
    width: 780px;
    height: 1250px;
    display: flex;
    justify-content: center;
  }
  
  .phone-frame {
    position: relative;
    width: 760px;
    height: 1250px;
    background: #121520;
    border-radius: 58px;
    padding: 15px;
    box-shadow: 
      0 0 0 3px rgba(255, 255, 255, 0.18),
      0 0 0 7px #1A1F2C,
      0 35px 90px -15px rgba(0, 0, 0, 0.95),
      0 15px 45px rgba(0, 0, 0, 0.8);
    overflow: hidden;
  }
  
  /* Specular Highlight Rim */
  .phone-frame::before {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 58px;
    padding: 2.5px;
    background: linear-gradient(135deg, rgba(255,255,255,0.45) 0%, rgba(255,255,255,0.06) 40%, rgba(255,255,255,0.25) 100%);
    -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    -webkit-mask-composite: xor;
    pointer-events: none;
    z-index: 50;
  }
  
  .screen-content {
    position: relative;
    width: 100%;
    height: 100%;
    background: #0A0D15;
    border-radius: 46px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  /* Status Bar */
  .status-bar {
    height: 56px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 36px;
    position: relative;
    z-index: 40;
    font-size: 15px;
    font-weight: 700;
    color: #F1F5F9;
  }
  
  /* Dynamic Island Pill */
  .dynamic-island {
    position: absolute;
    top: 12px;
    left: 50%;
    transform: translateX(-50%);
    width: 140px;
    height: 34px;
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
  
  .sensor-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #0A0F1D;
  }
  
  /* Bottom Navigation Bar for Inner Screens */
  .bottom-nav {
    height: 80px;
    background: rgba(13, 17, 27, 0.85);
    backdrop-filter: blur(20px);
    border-top: 1px solid rgba(255, 255, 255, 0.08);
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 0 20px 10px;
    position: relative;
    z-index: 40;
  }
  
  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    font-weight: 700;
    color: #64748B;
  }
  
  .nav-item.active {
    color: #38BDF8;
  }
  
  .nav-item svg {
    width: 24px;
    height: 24px;
  }
  
  /* Floating Glass Badges with Depth */
  .float-badge {
    position: absolute;
    z-index: 60;
    backdrop-filter: blur(28px);
    border-radius: 22px;
    padding: 18px 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 
      0 24px 50px -10px rgba(0, 0, 0, 0.8),
      0 0 25px rgba(255, 255, 255, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.18);
    background: rgba(16, 21, 34, 0.82);
  }
  
  .badge-icon-box {
    width: 48px;
    height: 48px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
  }
  
  .badge-text-primary {
    font-size: 17px;
    font-weight: 800;
    color: #FFFFFF;
    line-height: 1.2;
  }
  
  .badge-text-secondary {
    font-size: 13.5px;
    font-weight: 600;
    color: #94A3B8;
    line-height: 1.2;
    margin-top: 4px;
  }
</style>
</head>
"""

SCREENS_DATA = [
    # -------------------------------------------------------------
    # SCREEN 1: 0ms INSTANT INTERCEPTION
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_1_instant_lock",
        "theme_color_1": "#4F46E5",
        "theme_color_2": "#06B6D4",
        "badge_text": "⚡ ZERO-DELAY SECURITY",
        "badge_bg": "rgba(79, 70, 229, 0.2)",
        "badge_border": "rgba(56, 189, 248, 0.4)",
        "badge_color": "#38BDF8",
        "title": "Lock Any App With<br><span style='background:linear-gradient(135deg,#38BDF8 0%,#818CF8 50%,#C084FC 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>0ms Instant Interception</span>",
        "subtitle": "Window-level security blocks unauthorized launches before a single frame leaks.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "⚡",
            "icon_bg": "linear-gradient(135deg, #0284C7, #38BDF8)",
            "title": "0ms Zero Delay",
            "sub": "Accessibility Intercept Engine"
        },
        "float_right": {
            "bottom": "110px",
            "right": "-80px",
            "icon": "🛡️",
            "icon_bg": "linear-gradient(135deg, #4338CA, #6366F1)",
            "title": "No Screen Glitches",
            "sub": "Zero-Leak Window Shield"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; align-items:center; justify-content:space-between; padding: 25px 36px 40px;">
            <!-- Top App Shield Pill -->
            <div style="display:flex; align-items:center; gap: 8px; background: rgba(56,189,248,0.12); border: 1px solid rgba(56,189,248,0.25); padding: 8px 18px; border-radius: 999px; font-size: 13px; font-weight: 700; color: #38BDF8;">
              <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/></svg>
              APP LOCKER HARDWARE SHIELD
            </div>
            
            <!-- Hero Locked App Graphic -->
            <div style="display:flex; flex-direction:column; align-items:center; margin: 15px 0 10px;">
              <div style="position:relative; width: 105px; height: 105px; border-radius: 28px; background: linear-gradient(135deg, #25D366, #128C7E); display:flex; align-items:center; justify-content:center; box-shadow: 0 16px 36px rgba(37, 211, 102, 0.4); margin-bottom: 18px;">
                <svg width="58" height="58" viewBox="0 0 24 24" fill="white"><path d="M12.04 2c-5.46 0-9.91 4.45-9.91 9.91 0 1.75.46 3.45 1.32 4.95L2.05 22l5.25-1.38c1.45.79 3.08 1.21 4.74 1.21 5.46 0 9.91-4.45 9.91-9.91 0-2.65-1.03-5.14-2.9-7.01A9.816 9.816 0 0012.04 2z"/></svg>
                <div style="position:absolute; bottom: -8px; right: -8px; width: 36px; height: 36px; border-radius: 50%; background: #0F172A; border: 2.5px solid #38BDF8; display:flex; align-items:center; justify-content:center;">
                  <svg width="18" height="18" fill="none" stroke="#38BDF8" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                </div>
              </div>
              
              <div style="font-size: 28px; font-weight: 900; color: #FFFFFF; margin-bottom: 6px;">WhatsApp Locked</div>
              <div style="font-size: 16px; font-weight: 600; color: #94A3B8;">Enter master PIN or touch sensor</div>
            </div>
            
            <!-- Glowing PIN Indicators -->
            <div style="display:flex; gap: 24px; margin: 15px 0 25px;">
              <div style="width: 22px; height: 22px; border-radius: 50%; background: #38BDF8; box-shadow: 0 0 22px #38BDF8;"></div>
              <div style="width: 22px; height: 22px; border-radius: 50%; background: #38BDF8; box-shadow: 0 0 22px #38BDF8;"></div>
              <div style="width: 22px; height: 22px; border-radius: 50%; background: #38BDF8; box-shadow: 0 0 22px #38BDF8;"></div>
              <div style="width: 22px; height: 22px; border-radius: 50%; background: rgba(255,255,255,0.12); border: 2px solid rgba(255,255,255,0.35);"></div>
            </div>
            
            <!-- Full Numeric Keypad with Letters -->
            <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap: 16px 24px; width: 100%; max-width: 440px;">
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">1</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">&nbsp;</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">2</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">ABC</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">3</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">DEF</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">4</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">GHI</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">5</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">JKL</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">6</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">MNO</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">7</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">PQRS</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">8</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">TUV</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">9</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">WXYZ</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(56,189,248,0.14); border: 1.5px solid rgba(56,189,248,0.35); display:flex; align-items:center; justify-content:center; color: #38BDF8;">
                <svg width="34" height="34" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 004.07 9m4.896 11.834A8.966 8.966 0 0112 21c1.294 0 2.531-.274 3.65-.768"/></svg>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; flex-direction:column; align-items:center; justify-content:center; color: #FFFFFF;">
                <span style="font-size: 28px; font-weight: 800; line-height: 1;">0</span>
                <span style="font-size: 10px; font-weight: 700; color: #64748B; margin-top: 2px;">+</span>
              </div>
              <div style="height: 74px; border-radius: 37px; background: rgba(255,255,255,0.06); border: 1.5px solid rgba(255,255,255,0.12); display:flex; align-items:center; justify-content:center; color: #94A3B8;">
                <svg width="30" height="30" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.414-6.414a2 2 0 011.414-.586H19a2 2 0 012 2v10a2 2 0 01-2 2h-8.172a2 2 0 01-1.414-.586L3 12z"/></svg>
              </div>
            </div>
            
            <div style="font-size: 14px; font-weight: 600; color: #64748B;">Forgot PIN? Tap for Recovery</div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 2: SILENT INTRUDER SELFIE
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_2_intruder_selfie",
        "theme_color_1": "#EF4444",
        "theme_color_2": "#F59E0B",
        "badge_text": "📸 SILENT INTRUDER DETECTOR",
        "badge_bg": "rgba(239, 68, 68, 0.2)",
        "badge_border": "rgba(248, 113, 113, 0.4)",
        "badge_color": "#F87171",
        "title": "Catch Intruders<br><span style='background:linear-gradient(135deg,#F87171 0%,#F59E0B 50%,#FCD34D 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Silent Front-Camera Selfie</span>",
        "subtitle": "Silently captures a high-resolution photo on wrong PIN or Pattern entries.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🤫",
            "icon_bg": "linear-gradient(135deg, #DC2626, #EF4444)",
            "title": "100% Silent Snap",
            "sub": "No Flash • No Shutter Sound"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "🚨",
            "icon_bg": "linear-gradient(135deg, #D97706, #F59E0B)",
            "title": "Intruder Alerted",
            "sub": "Timestamp & App Logged"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <!-- Top Alert Card -->
            <div>
              <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom: 16px;">
                <div style="display:flex; align-items:center; gap: 10px;">
                  <div style="width: 14px; height: 14px; border-radius: 50%; background: #EF4444; box-shadow: 0 0 14px #EF4444;"></div>
                  <div style="font-size: 20px; font-weight: 900; color: #EF4444;">INTRUSION DETECTED</div>
                </div>
                <div style="background: rgba(239,68,68,0.2); border: 1px solid rgba(239,68,68,0.4); padding: 6px 14px; border-radius: 8px; font-size: 13px; font-weight: 800; color: #FCA5A5;">3 Failed PINs</div>
              </div>
              
              <!-- Target Framing Camera Viewfinder -->
              <div style="position:relative; width: 100%; height: 510px; border-radius: 32px; background: #131722; overflow:hidden; border: 2.5px solid rgba(239, 68, 68, 0.45); box-shadow: 0 20px 50px rgba(239,68,68,0.25); display:flex; align-items:center; justify-content:center;">
                <div style="position:absolute; inset: 0; background: radial-gradient(circle at center, rgba(239,68,68,0.18) 0%, transparent 75%);"></div>
                
                <!-- Crosshair brackets -->
                <div style="position:absolute; top: 35px; left: 35px; width: 44px; height: 44px; border-top: 3.5px solid #EF4444; border-left: 3.5px solid #EF4444;"></div>
                <div style="position:absolute; top: 35px; right: 35px; width: 44px; height: 44px; border-top: 3.5px solid #EF4444; border-right: 3.5px solid #EF4444;"></div>
                <div style="position:absolute; bottom: 35px; left: 35px; width: 44px; height: 44px; border-bottom: 3.5px solid #EF4444; border-left: 3.5px solid #EF4444;"></div>
                <div style="position:absolute; bottom: 35px; right: 35px; width: 44px; height: 44px; border-bottom: 3.5px solid #EF4444; border-right: 3.5px solid #EF4444;"></div>
                
                <!-- Silhouette avatar -->
                <div style="display:flex; flex-direction:column; align-items:center;">
                  <div style="width: 170px; height: 170px; border-radius: 50%; background: linear-gradient(180deg, #334155, #1E293B); border: 3.5px solid rgba(239,68,68,0.85); display:flex; align-items:center; justify-content:center; box-shadow: 0 0 40px rgba(239,68,68,0.45);">
                    <svg width="105" height="105" viewBox="0 0 24 24" fill="#94A3B8"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                  </div>
                  <div style="margin-top: 18px; background: #EF4444; color: white; font-weight: 900; font-size: 14px; padding: 6px 18px; border-radius: 8px; letter-spacing: 0.1em; box-shadow: 0 4px 15px rgba(239,68,68,0.5);">FACE RECOGNIZED • 99.4%</div>
                </div>
                
                <!-- Snapshot Watermark -->
                <div style="position:absolute; bottom: 20px; left: 24px; font-size: 13px; color: #E2E8F0; font-family: monospace; font-weight: 600;">REC • TODAY 14:28:05.184 • SNAPCHAT</div>
                <div style="position:absolute; top: 20px; right: 24px; font-size: 12px; color: #EF4444; font-weight: 800; letter-spacing: 0.1em;">● SILENT MODE</div>
              </div>
            </div>
            
            <!-- Metadata Cards -->
            <div style="background: rgba(255,255,255,0.05); border: 1.5px solid rgba(255,255,255,0.1); border-radius: 22px; padding: 20px 24px; display:flex; flex-direction:column; gap: 14px;">
              <div style="display:flex; justify-content:space-between; font-size: 15px;">
                <span style="color:#94A3B8; font-weight: 600;">Target Application</span>
                <span style="color:#FFFFFF; font-weight:800;">Snapchat & Private Gallery</span>
              </div>
              <div style="display:flex; justify-content:space-between; font-size: 15px;">
                <span style="color:#94A3B8; font-weight: 600;">Capture Mode</span>
                <span style="color:#38BDF8; font-weight:800;">Silent Front Camera (Ultra-HD)</span>
              </div>
              <div style="display:flex; justify-content:space-between; font-size: 15px;">
                <span style="color:#94A3B8; font-weight: 600;">Storage Protection</span>
                <span style="color:#34D399; font-weight:800;">Hardware AES-256 GCM KeyStore</span>
              </div>
            </div>
            
            <!-- Action Buttons -->
            <div style="display:flex; gap: 14px;">
              <div style="flex:1; background: #EF4444; border-radius: 16px; padding: 14px 0; text-align:center; font-size: 15px; font-weight: 800; color: white;">Inspect Evidence</div>
              <div style="flex:1; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); border-radius: 16px; padding: 14px 0; text-align:center; font-size: 15px; font-weight: 800; color: white;">Delete & Shred</div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 3: BIOMETRIC SHIELD VAULT
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_3_biometric_vault",
        "theme_color_1": "#10B981",
        "theme_color_2": "#06B6D4",
        "badge_text": "👁️ TAP-TO-REVEAL BIOMETRICS",
        "badge_bg": "rgba(16, 185, 129, 0.2)",
        "badge_border": "rgba(52, 211, 153, 0.4)",
        "badge_color": "#34D399",
        "title": "Intruder Evidence With<br><span style='background:linear-gradient(135deg,#34D399 0%,#06B6D4 50%,#38BDF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Tap-To-Reveal Shield</span>",
        "subtitle": "Sensitive evidence photos stay cryptographically blurred until you verify your biometrics.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🔒",
            "icon_bg": "linear-gradient(135deg, #059669, #10B981)",
            "title": "Blurred by Default",
            "sub": "Zero Snooping in Logs"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "👆",
            "icon_bg": "linear-gradient(135deg, #0891B2, #06B6D4)",
            "title": "Touch ID & Face ID",
            "sub": "Instant Biometric Reveal"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 22px;">
                <div>
                  <div style="font-size: 26px; font-weight: 900; color: #FFFFFF;">Intruder Vault</div>
                  <div style="font-size: 14px; color: #94A3B8; margin-top: 3px;">Protected by Biometric KeyStore</div>
                </div>
                <div style="background: rgba(16,185,129,0.18); border: 1.5px solid rgba(16,185,129,0.35); padding: 8px 16px; border-radius: 999px; font-size: 13px; font-weight: 800; color: #34D399;">3 Encrypted Logs</div>
              </div>
              
              <!-- Big Blurred Evidence Card with Fingerprint Scanner -->
              <div style="position:relative; width: 100%; height: 490px; border-radius: 32px; background: linear-gradient(135deg, #121F2A, #182C3D); border: 2.5px solid rgba(52, 211, 153, 0.45); box-shadow: 0 20px 50px rgba(16,185,129,0.25); overflow:hidden; display:flex; flex-direction:column; align-items:center; justify-content:center; margin-bottom: 20px;">
                <!-- Frosted Blur Overlay -->
                <div style="position:absolute; inset: 0; backdrop-filter: blur(32px); background: rgba(10, 15, 26, 0.78);"></div>
                
                <!-- Concentric Fingerprint Pulse -->
                <div style="position:relative; z-index: 10; display:flex; flex-direction:column; align-items:center;">
                  <div style="position:relative; width: 120px; height: 120px; border-radius: 50%; background: rgba(16, 185, 129, 0.16); border: 2.5px solid #34D399; display:flex; align-items:center; justify-content:center; box-shadow: 0 0 45px rgba(52,211,153,0.55); margin-bottom: 20px;">
                    <svg width="68" height="68" fill="none" stroke="#34D399" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 004.07 9m4.896 11.834A8.966 8.966 0 0112 21c1.294 0 2.531-.274 3.65-.768"/></svg>
                  </div>
                  <div style="font-size: 22px; font-weight: 900; color: #FFFFFF; margin-bottom: 6px;">Tap to Reveal Photo</div>
                  <div style="font-size: 15px; font-weight: 700; color: #34D399;">Fingerprint or Face Authentication</div>
                  <div style="margin-top: 14px; background: rgba(52,211,153,0.14); border: 1px solid rgba(52,211,153,0.3); padding: 6px 16px; border-radius: 999px; font-size: 12px; font-weight: 700; color: #A7F3D0;">HARDWARE KEYSTORE CERTIFIED ✓</div>
                </div>
                
                <div style="position:absolute; bottom: 20px; font-size: 12px; color: #64748B; font-weight: 700; letter-spacing: 0.08em;">ENCRYPTED AT REST VIA AES-256 GCM</div>
              </div>
              
              <!-- Secondary Shielded Log Row -->
              <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 18px 22px; display:flex; align-items:center; justify-content:space-between;">
                <div style="display:flex; align-items:center; gap: 16px;">
                  <div style="width: 50px; height: 50px; border-radius: 14px; background: rgba(239,68,68,0.15); border: 1px solid rgba(239,68,68,0.3); display:flex; align-items:center; justify-content:center; font-size: 24px;">📸</div>
                  <div>
                    <div style="font-size: 16px; font-weight: 800; color: #FFFFFF;">Instagram Attempt Blocked</div>
                    <div style="font-size: 13px; color: #94A3B8; margin-top: 2px;">Today, 11:15 AM • 2 Wrong PINs</div>
                  </div>
                </div>
                <div style="background: rgba(52,211,153,0.18); border: 1px solid rgba(52,211,153,0.35); padding: 6px 14px; border-radius: 8px; color: #34D399; font-size: 13px; font-weight: 800;">Shielded 🔒</div>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 4: HARDWARE-BACKED AES-256 GCM
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_4_hardware_encryption",
        "theme_color_1": "#8B5CF6",
        "theme_color_2": "#6366F1",
        "badge_text": "🛡️ HARDWARE-BACKED ENCLAVE",
        "badge_bg": "rgba(139, 92, 246, 0.2)",
        "badge_border": "rgba(167, 139, 250, 0.4)",
        "badge_color": "#A78BFA",
        "title": "Military-Grade<br><span style='background:linear-gradient(135deg,#A78BFA 0%,#818CF8 50%,#38BDF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>AES-256 Hardware Vault</span>",
        "subtitle": "Encryption keys generated and stored inside the Android KeyStore TEE hardware.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🔐",
            "icon_bg": "linear-gradient(135deg, #7C3AED, #8B5CF6)",
            "title": "Android KeyStore",
            "sub": "Isolated Hardware Enclave"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "🌐",
            "icon_bg": "linear-gradient(135deg, #4F46E5, #6366F1)",
            "title": "100% Offline Vault",
            "sub": "Zero Cloud Data Transmission"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <div style="font-size: 26px; font-weight: 900; color: #FFFFFF; margin-bottom: 4px;">Hardware Security Core</div>
              <div style="font-size: 14px; color: #94A3B8; margin-bottom: 24px;">TEE Cryptographic Subsystem Active</div>
              
              <!-- Central Hardware Microprocessor Chip Graphic -->
              <div style="position:relative; width: 100%; height: 360px; border-radius: 32px; background: linear-gradient(135deg, #17152B, #231B40); border: 2.5px solid rgba(167, 139, 250, 0.45); box-shadow: 0 20px 50px rgba(139,92,246,0.3); display:flex; flex-direction:column; align-items:center; justify-content:center; margin-bottom: 22px;">
                <!-- Glowing Chip Core -->
                <div style="width: 110px; height: 110px; border-radius: 24px; background: #2E1065; border: 2.5px solid #A78BFA; display:flex; align-items:center; justify-content:center; box-shadow: 0 0 40px rgba(167,139,250,0.6); margin-bottom: 18px;">
                  <svg width="60" height="60" fill="none" stroke="#C4B5FD" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"/></svg>
                </div>
                
                <div style="font-size: 24px; font-weight: 900; color: #FFFFFF; letter-spacing: 0.05em;">TITAN TEE / KEYSTORE</div>
                <div style="font-size: 15px; font-weight: 800; color: #A78BFA; margin-top: 4px;">AES-256-GCM Cryptographic Hardware</div>
                <div style="margin-top: 14px; background: rgba(167,139,250,0.18); border: 1px solid rgba(167,139,250,0.35); padding: 6px 18px; border-radius: 999px; font-size: 12px; font-family: monospace; color: #DDD6FE;">KEY: 9f8a...3b21 [HARDWARE-LOCKED]</div>
              </div>
              
              <!-- 4 Security Audit Rows -->
              <div style="display:flex; flex-direction:column; gap: 12px;">
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 16px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 15px; color:#E2E8F0; font-weight:700;">Data at Rest Encryption</span>
                  <span style="color:#34D399; font-weight:900; font-size: 14px;">✓ 256-Bit AES GCM</span>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 16px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 15px; color:#E2E8F0; font-weight:700;">RAM Key Leak Prevention</span>
                  <span style="color:#34D399; font-weight:900; font-size: 14px;">✓ Zero-Leak Memory</span>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 16px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 15px; color:#E2E8F0; font-weight:700;">External Cloud Exposure</span>
                  <span style="color:#38BDF8; font-weight:900; font-size: 14px;">0% (100% On-Device)</span>
                </div>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 5: VERSATILE LOCK MODES
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_5_lock_modes",
        "theme_color_1": "#F43F5E",
        "theme_color_2": "#FB923C",
        "badge_text": "✨ CUSTOMIZABLE ACCESS",
        "badge_bg": "rgba(244, 63, 94, 0.2)",
        "badge_border": "rgba(251, 113, 133, 0.4)",
        "badge_color": "#FB7185",
        "title": "Versatile Protection<br><span style='background:linear-gradient(135deg,#FB7185 0%,#FB923C 50%,#FCD34D 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>PIN, Pattern & Password</span>",
        "subtitle": "Choose 4 or 6-digit PIN, fluid neon pattern, alphanumeric password, or biometrics.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🔢",
            "icon_bg": "linear-gradient(135deg, #E11D48, #F43F5E)",
            "title": "4 & 6-Digit PIN",
            "sub": "Scrambled Keypad Option"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "🔮",
            "icon_bg": "linear-gradient(135deg, #EA580C, #FB923C)",
            "title": "Stealth Pattern",
            "sub": "Hide Pattern Trail Lines"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <div style="font-size: 26px; font-weight: 900; color: #FFFFFF; margin-bottom: 18px;">Select Lock Style</div>
              
              <!-- Tab selector -->
              <div style="display:flex; background: rgba(255,255,255,0.06); padding: 6px; border-radius: 18px; margin-bottom: 24px;">
                <div style="flex:1; text-align:center; padding: 12px 0; font-size: 14px; font-weight: 700; color: #94A3B8;">PIN</div>
                <div style="flex:1; text-align:center; padding: 12px 0; font-size: 14px; font-weight: 900; color: #FFFFFF; background: linear-gradient(135deg, #F43F5E, #FB923C); border-radius: 14px; box-shadow: 0 4px 20px rgba(244,63,94,0.45);">PATTERN</div>
                <div style="flex:1; text-align:center; padding: 12px 0; font-size: 14px; font-weight: 700; color: #94A3B8;">PASSWORD</div>
              </div>
              
              <!-- 3x3 Luminous Pattern Grid Container -->
              <div style="position:relative; width: 100%; height: 460px; border-radius: 32px; background: #161726; border: 2.5px solid rgba(244, 63, 94, 0.4); box-shadow: 0 20px 50px rgba(244,63,94,0.25); display:flex; flex-direction:column; align-items:center; justify-content:center; margin-bottom: 22px;">
                <!-- Connecting Neon Trail SVG -->
                <svg style="position:absolute; width: 360px; height: 360px;" viewBox="0 0 360 360">
                  <defs>
                    <linearGradient id="trailGrad2" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#FB7185"/>
                      <stop offset="100%" stop-color="#FB923C"/>
                    </linearGradient>
                  </defs>
                  <path d="M60 60 L180 60 L300 180 L180 300" fill="none" stroke="url(#trailGrad2)" stroke-width="8" stroke-linecap="round" stroke-linejoin="round" style="filter: drop-shadow(0 0 16px #FB7185);"/>
                </svg>
                
                <!-- 9 Dots Matrix -->
                <div style="display:grid; grid-template-columns: repeat(3, 120px); grid-template-rows: repeat(3, 120px); place-items: center; position:relative; z-index: 10;">
                  <!-- Node 1 (Active) -->
                  <div style="width: 28px; height: 28px; border-radius: 50%; background: #FB7185; box-shadow: 0 0 24px #FB7185; border: 3.5px solid #FFF;"></div>
                  <!-- Node 2 (Active) -->
                  <div style="width: 28px; height: 28px; border-radius: 50%; background: #FB7185; box-shadow: 0 0 24px #FB7185; border: 3.5px solid #FFF;"></div>
                  <!-- Node 3 -->
                  <div style="width: 20px; height: 20px; border-radius: 50%; background: rgba(255,255,255,0.25);"></div>
                  <!-- Node 4 -->
                  <div style="width: 20px; height: 20px; border-radius: 50%; background: rgba(255,255,255,0.25);"></div>
                  <!-- Node 5 -->
                  <div style="width: 20px; height: 20px; border-radius: 50%; background: rgba(255,255,255,0.25);"></div>
                  <!-- Node 6 (Active) -->
                  <div style="width: 28px; height: 28px; border-radius: 50%; background: #FB923C; box-shadow: 0 0 24px #FB923C; border: 3.5px solid #FFF;"></div>
                  <!-- Node 7 -->
                  <div style="width: 20px; height: 20px; border-radius: 50%; background: rgba(255,255,255,0.25);"></div>
                  <!-- Node 8 (Active) -->
                  <div style="width: 28px; height: 28px; border-radius: 50%; background: #FB923C; box-shadow: 0 0 24px #FB923C; border: 3.5px solid #FFF;"></div>
                  <!-- Node 9 -->
                  <div style="width: 20px; height: 20px; border-radius: 50%; background: rgba(255,255,255,0.25);"></div>
                </div>
              </div>
              
              <!-- Option Switches -->
              <div style="display:flex; flex-direction:column; gap: 10px;">
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 16px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 15px; font-weight: 700; color: #FFFFFF;">Biometric Quick Unlock</span>
                  <div style="width: 52px; height: 30px; background: #FB7185; border-radius: 15px; position:relative; display:flex; align-items:center; justify-content:flex-end; padding: 3px;">
                    <div style="width: 24px; height: 24px; border-radius: 50%; background: white;"></div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 6: SMART DOUBLE-LOCK ADVISOR
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_6_double_lock_advisor",
        "theme_color_1": "#0EA5E9",
        "theme_color_2": "#2DD4BF",
        "badge_text": "🔄 ZERO-LOOP SMART ADVISOR",
        "badge_bg": "rgba(14, 165, 233, 0.2)",
        "badge_border": "rgba(56, 189, 248, 0.4)",
        "badge_color": "#38BDF8",
        "title": "Smart Compatibility<br><span style='background:linear-gradient(135deg,#38BDF8 0%,#2DD4BF 50%,#34D399 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Zero-Loop Protection</span>",
        "subtitle": "Intelligently prevents annoying double-authentication loops for banking & biometric apps.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🧠",
            "icon_bg": "linear-gradient(135deg, #0284C7, #0EA5E9)",
            "title": "AI Lock Advisor",
            "sub": "Detects Native App Biometrics"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "⏱️",
            "icon_bg": "linear-gradient(135deg, #0D9488, #2DD4BF)",
            "title": "Per-App Relock Timers",
            "sub": "Immediate, 30s, 1m, or 5m"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <div style="font-size: 26px; font-weight: 900; color: #FFFFFF; margin-bottom: 4px;">Double-Lock Advisor</div>
              <div style="font-size: 14px; color: #94A3B8; margin-bottom: 22px;">Smart Conflict Prevention Active</div>
              
              <!-- Smart Advisor Banner -->
              <div style="background: linear-gradient(135deg, rgba(14,165,233,0.18), rgba(45,212,191,0.12)); border: 2px solid rgba(56,189,248,0.45); border-radius: 24px; padding: 20px; margin-bottom: 22px;">
                <div style="display:flex; align-items:center; gap: 14px; margin-bottom: 12px;">
                  <div style="font-size: 28px;">💡</div>
                  <div style="font-size: 17px; font-weight: 900; color: #38BDF8;">Optimization Active</div>
                </div>
                <div style="font-size: 14.5px; color: #E2E8F0; line-height: 1.45;">WhatsApp and Google Wallet have internal biometric security active. App Locker automatically adjusted relock timing to eliminate annoying double authentication prompts.</div>
              </div>
              
              <!-- App Optimization Rows -->
              <div style="display:flex; flex-direction:column; gap: 14px;">
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 18px 20px; display:flex; align-items:center; justify-content:space-between;">
                  <div style="display:flex; align-items:center; gap: 16px;">
                    <div style="width: 50px; height: 50px; border-radius: 14px; background: #25D366; display:flex; align-items:center; justify-content:center; font-size: 26px;">💬</div>
                    <div>
                      <div style="font-size: 16px; font-weight: 800; color: #FFFFFF;">WhatsApp</div>
                      <div style="font-size: 13px; color: #34D399; font-weight:600;">Native Fingerprint Detected</div>
                    </div>
                  </div>
                  <div style="background: rgba(14,165,233,0.2); border: 1px solid rgba(14,165,233,0.4); color: #38BDF8; font-weight: 800; font-size: 13px; padding: 8px 14px; border-radius: 10px;">Delay: 30s ✓</div>
                </div>
                
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 18px 20px; display:flex; align-items:center; justify-content:space-between;">
                  <div style="display:flex; align-items:center; gap: 16px;">
                    <div style="width: 50px; height: 50px; border-radius: 14px; background: #4285F4; display:flex; align-items:center; justify-content:center; font-size: 26px;">💳</div>
                    <div>
                      <div style="font-size: 16px; font-weight: 800; color: #FFFFFF;">Google Wallet</div>
                      <div style="font-size: 13px; color: #34D399; font-weight:600;">System Biometrics Detected</div>
                    </div>
                  </div>
                  <div style="background: rgba(14,165,233,0.2); border: 1px solid rgba(14,165,233,0.4); color: #38BDF8; font-weight: 800; font-size: 13px; padding: 8px 14px; border-radius: 10px;">Delay: 1m ✓</div>
                </div>
                
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 18px 20px; display:flex; align-items:center; justify-content:space-between;">
                  <div style="display:flex; align-items:center; gap: 16px;">
                    <div style="width: 50px; height: 50px; border-radius: 14px; background: #E1306C; display:flex; align-items:center; justify-content:center; font-size: 26px;">📸</div>
                    <div>
                      <div style="font-size: 16px; font-weight: 800; color: #FFFFFF;">Instagram</div>
                      <div style="font-size: 13px; color: #94A3B8; font-weight:600;">Standard Instant Protection</div>
                    </div>
                  </div>
                  <div style="background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.15); color: #FFFFFF; font-weight: 800; font-size: 13px; padding: 8px 14px; border-radius: 10px;">Immediate</div>
                </div>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 7: FORENSIC FILE SHREDDER
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_7_forensic_shredder",
        "theme_color_1": "#E11D48",
        "theme_color_2": "#FF007A",
        "badge_text": "🗑️ MILITARY DATA SANITIZATION",
        "badge_bg": "rgba(225, 29, 72, 0.2)",
        "badge_border": "rgba(251, 113, 133, 0.4)",
        "badge_color": "#FB7185",
        "title": "Permanent Deletion<br><span style='background:linear-gradient(135deg,#FB7185 0%,#FF007A 50%,#C084FC 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>3-Pass Forensic Shredder</span>",
        "subtitle": "Overwrites storage blocks with cryptographic random noise before unlinking.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "⚡",
            "icon_bg": "linear-gradient(135deg, #BE123C, #E11D48)",
            "title": "DoD 5220.22-M",
            "sub": "Sanitization Standard"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "🛡️",
            "icon_bg": "linear-gradient(135deg, #C026D3, #E879F9)",
            "title": "Zero Remanence",
            "sub": "Unrecoverable By Forensic Tools"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <div style="font-size: 26px; font-weight: 900; color: #FFFFFF; margin-bottom: 4px;">Forensic File Shredder</div>
              <div style="font-size: 14px; color: #94A3B8; margin-bottom: 22px;">Multi-Pass Cryptographic Sanitizer</div>
              
              <!-- Circular Shredder Progress Gauge -->
              <div style="position:relative; width: 100%; height: 380px; border-radius: 32px; background: #19121E; border: 2.5px solid rgba(225, 29, 72, 0.45); box-shadow: 0 20px 50px rgba(225,29,72,0.3); display:flex; flex-direction:column; align-items:center; justify-content:center; margin-bottom: 22px;">
                <!-- Radial Gauge Ring -->
                <div style="position:relative; width: 160px; height: 160px; border-radius: 50%; border: 10px solid rgba(225,29,72,0.25); border-top-color: #FB7185; border-right-color: #FF007A; display:flex; flex-direction:column; align-items:center; justify-content:center; box-shadow: 0 0 35px rgba(251,113,133,0.35); margin-bottom: 16px;">
                  <div style="font-size: 38px; font-weight: 900; color: #FFFFFF;">100%</div>
                  <div style="font-size: 12px; font-weight: 800; color: #FB7185; letter-spacing: 0.1em;">PURGED</div>
                </div>
                <div style="font-size: 20px; font-weight: 900; color: #FFFFFF;">Physical Flash Sectors Overwritten</div>
                <div style="font-size: 13px; font-family: monospace; color: #94A3B8; margin-top: 4px;">SECTOR: 0x8F3A41B... SANITIZED</div>
                <div style="margin-top: 14px; background: rgba(225,29,72,0.2); border: 1px solid rgba(225,29,72,0.4); padding: 5px 16px; border-radius: 999px; font-size: 12px; font-weight: 800; color: #FDA4AF;">UNRECOVERABLE EVIDENCE DESTRUCTION ✓</div>
              </div>
              
              <!-- 3 Passes Checklist -->
              <div style="display:flex; flex-direction:column; gap: 10px;">
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 15px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 14.5px; color:#E2E8F0; font-weight:700;">Pass 1: Cryptographic PRNG Noise</span>
                  <span style="color:#34D399; font-weight:900; font-size: 13.5px;">Complete ✓</span>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 15px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 14.5px; color:#E2E8F0; font-weight:700;">Pass 2: Inverted Bit Pattern</span>
                  <span style="color:#34D399; font-weight:900; font-size: 13.5px;">Complete ✓</span>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 18px; padding: 15px 20px; display:flex; justify-content:space-between; align-items:center;">
                  <span style="font-size: 14.5px; color:#E2E8F0; font-weight:700;">Pass 3: Cryptographic Zero-Fill</span>
                  <span style="color:#34D399; font-weight:900; font-size: 13.5px;">Complete ✓</span>
                </div>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    },

    # -------------------------------------------------------------
    # SCREEN 8: CENTRAL COMMAND HUB
    # -------------------------------------------------------------
    {
        "id": "playstore_phone_screenshot_8_security_hub",
        "theme_color_1": "#3B82F6",
        "theme_color_2": "#10B981",
        "badge_text": "🎛️ TOTAL PRIVACY DASHBOARD",
        "badge_bg": "rgba(59, 130, 246, 0.2)",
        "badge_border": "rgba(96, 165, 250, 0.4)",
        "badge_color": "#60A5FA",
        "title": "Command Center<br><span style='background:linear-gradient(135deg,#60A5FA 0%,#34D399 50%,#38BDF8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;'>Complete Privacy Control</span>",
        "subtitle": "Monitor your security score, arm intruder detection, and prevent app uninstallation.",
        "float_left": {
            "top": "160px",
            "left": "-85px",
            "icon": "🛡️",
            "icon_bg": "linear-gradient(135deg, #1D4ED8, #3B82F6)",
            "title": "Anti-Uninstall Shield",
            "sub": "Blocks Settings & Play Store"
        },
        "float_right": {
            "bottom": "100px",
            "right": "-80px",
            "icon": "📊",
            "icon_bg": "linear-gradient(135deg, #059669, #10B981)",
            "title": "98% Security Score",
            "sub": "Zero Vulnerabilities Detected"
        },
        "screen_html": """
          <div style="flex:1; display:flex; flex-direction:column; justify-content:space-between; padding: 20px 30px 24px;">
            <div>
              <!-- Radial Security Health Score Card -->
              <div style="background: linear-gradient(135deg, #152438, #13332D); border: 2.5px solid rgba(56, 189, 248, 0.4); border-radius: 28px; padding: 24px; display:flex; align-items:center; gap: 22px; margin-bottom: 22px; box-shadow: 0 16px 40px rgba(16,185,129,0.2);">
                <div style="position:relative; width: 90px; height: 90px; border-radius: 50%; border: 7px solid #10B981; display:flex; flex-direction:column; align-items:center; justify-content:center; box-shadow: 0 0 25px rgba(16,185,129,0.45);">
                  <div style="font-size: 24px; font-weight: 900; color: #FFFFFF;">98%</div>
                </div>
                <div>
                  <div style="font-size: 21px; font-weight: 900; color: #FFFFFF;">Security Health: Excellent</div>
                  <div style="font-size: 14px; color: #34D399; font-weight: 700; margin-top: 4px;">18 Apps Locked • Intruder Cam Armed</div>
                  <div style="font-size: 12px; color: #94A3B8; margin-top: 2px;">Android 16 API 36 Security Verified</div>
                </div>
              </div>
              
              <!-- 4 Quick Action Tile Grid -->
              <div style="display:grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 22px;">
                <div style="background: rgba(255,255,255,0.05); border: 1.5px solid rgba(255,255,255,0.1); border-radius: 22px; padding: 20px;">
                  <div style="font-size: 28px; margin-bottom: 10px;">🔒</div>
                  <div style="font-size: 18px; font-weight: 800; color: #FFFFFF;">App Lock</div>
                  <div style="font-size: 13px; color: #38BDF8; font-weight: 700; margin-top: 3px;">18 Apps Protected</div>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1.5px solid rgba(255,255,255,0.1); border-radius: 22px; padding: 20px;">
                  <div style="font-size: 28px; margin-bottom: 10px;">📸</div>
                  <div style="font-size: 18px; font-weight: 800; color: #FFFFFF;">Intruder Cam</div>
                  <div style="font-size: 13px; color: #34D399; font-weight: 700; margin-top: 3px;">Armed & Ready</div>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1.5px solid rgba(255,255,255,0.1); border-radius: 22px; padding: 20px;">
                  <div style="font-size: 28px; margin-bottom: 10px;">🛡️</div>
                  <div style="font-size: 18px; font-weight: 800; color: #FFFFFF;">Anti-Uninstall</div>
                  <div style="font-size: 13px; color: #A78BFA; font-weight: 700; margin-top: 3px;">Active & Shielded</div>
                </div>
                <div style="background: rgba(255,255,255,0.05); border: 1.5px solid rgba(255,255,255,0.1); border-radius: 22px; padding: 20px;">
                  <div style="font-size: 28px; margin-bottom: 10px;">⚡</div>
                  <div style="font-size: 18px; font-weight: 800; color: #FFFFFF;">Relock Delay</div>
                  <div style="font-size: 13px; color: #F59E0B; font-weight: 700; margin-top: 3px;">0ms Zero Latency</div>
                </div>
              </div>
              
              <!-- Realtime Activity Log Pill -->
              <div style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 20px; padding: 16px 20px; display:flex; align-items:center; justify-content:space-between;">
                <div style="display:flex; align-items:center; gap: 12px;">
                  <div style="width: 10px; height: 10px; border-radius: 50%; background: #10B981; box-shadow: 0 0 10px #10B981;"></div>
                  <span style="font-size: 14.5px; color: #E2E8F0; font-weight: 700;">System Shield Running in Background</span>
                </div>
                <span style="font-size: 13px; color: #94A3B8; font-weight:600;">0% Battery Drain</span>
              </div>
            </div>
            
            <!-- Bottom Navigation Bar -->
            <div class="bottom-nav">
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/></svg>
                <span>Apps</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Intruders</span>
              </div>
              <div class="nav-item active">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                <span>Security</span>
              </div>
              <div class="nav-item">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                <span>Settings</span>
              </div>
            </div>
          </div>
        """
    }
]

def generate_screen_html(data):
    return f"""{COMMON_HEAD}
<body>
  <div class="cyber-grid"></div>
  
  <!-- Glowing Atmospheric Background Orbs -->
  <div class="glow-orb" style="width: 850px; height: 850px; top: -140px; left: 115px; background: radial-gradient(circle, {data['theme_color_1']}44 0%, {data['theme_color_2']}22 50%, transparent 70%);"></div>
  <div class="glow-orb" style="width: 700px; height: 700px; bottom: 80px; left: 190px; background: radial-gradient(circle, {data['theme_color_2']}33 0%, transparent 70%);"></div>
  
  <!-- Pill Badge -->
  <div class="pill-badge" style="background:{data['badge_bg']}; border:1px solid {data['badge_border']}; color:{data['badge_color']};">
    {data['badge_text']}
  </div>
  
  <!-- Hero Title & Subtitle -->
  <h1 class="hero-title">{data['title']}</h1>
  <p class="hero-subtitle">{data['subtitle']}</p>
  
  <!-- Phone Stage -->
  <div class="device-stage">
    <!-- Floating Left Glass Badge -->
    <div class="float-badge" style="top:{data['float_left']['top']}; left:{data['float_left']['left']};">
      <div class="badge-icon-box" style="background:{data['float_left']['icon_bg']};">
        {data['float_left']['icon']}
      </div>
      <div>
        <div class="badge-text-primary">{data['float_left']['title']}</div>
        <div class="badge-text-secondary">{data['float_left']['sub']}</div>
      </div>
    </div>
    
    <!-- Floating Right Glass Badge -->
    <div class="float-badge" style="bottom:{data['float_right']['bottom']}; right:{data['float_right']['right']};">
      <div class="badge-icon-box" style="background:{data['float_right']['icon_bg']};">
        {data['float_right']['icon']}
      </div>
      <div>
        <div class="badge-text-primary">{data['float_right']['title']}</div>
        <div class="badge-text-secondary">{data['float_right']['sub']}</div>
      </div>
    </div>
    
    <!-- Phone Mockup Frame -->
    <div class="phone-frame">
      <div class="screen-content">
        <!-- Status Bar -->
        <div class="status-bar">
          <span>9:41</span>
          <div class="dynamic-island">
            <div class="sensor-dot"></div>
            <div class="camera-lens"></div>
          </div>
          <div style="display:flex; align-items:center; gap: 8px;">
            <svg width="18" height="13" viewBox="0 0 15 11" fill="#F1F5F9"><path d="M0 8.5h2v2H0v-2zm3.5-3h2v5h-2v-5zm3.5-3h2v8h-2v-8zm3.5-2.5h2v10.5h-2V0z"/></svg>
            <span style="font-size:13px; font-weight:800;">5G</span>
            <svg width="24" height="12" viewBox="0 0 22 11" fill="none" stroke="#F1F5F9"><rect x="0.5" y="0.5" width="18" height="10" rx="3"/><path d="M20 3.5v4" stroke-width="2"/><rect x="2" y="2" width="15" height="7" rx="1.5" fill="#10B981"/></svg>
          </div>
        </div>
        
        <!-- Bespoke Screen UI Content -->
        {data['screen_html']}
      </div>
    </div>
  </div>
</body>
</html>
"""

def main():
    temp_dir = Path("D:/app-locker/store_assets/.temp_html")
    temp_dir.mkdir(parents=True, exist_ok=True)
    
    print("Generating 8 Studio-Grade Showcase Images for Google Play Store...")
    
    for idx, screen in enumerate(SCREENS_DATA, start=1):
        html_content = generate_screen_html(screen)
        html_file = temp_dir / f"{screen['id']}.html"
        raw_png = temp_dir / f"{screen['id']}_raw.png"
        final_png = OUTPUT_DIR / f"{screen['id']}.png"
        
        html_file.write_text(html_content, encoding="utf-8")
        
        file_url = f"file:///{html_file.resolve().as_posix()}"
        print(f"[{idx}/8] Rendering {final_png.name}...")
        
        # Call Edge Headless
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
        
        # Validate and convert with Pillow to guarantee 1080x1920 24-bit RGB PNG (no alpha)
        with Image.open(raw_png) as img:
            img = img.crop((0, 0, 1080, 1920))
            rgb_img = img.convert("RGB")
            rgb_img.save(final_png, "PNG", optimize=True)
            
        file_size_kb = final_png.stat().st_size / 1024
        print(f"      [OK] Generated: {final_png.name} ({file_size_kb:.1f} KB, RGB, 1080x1920)")
        
    print("\nAll 8 Play Console screenshots created successfully!")

if __name__ == "__main__":
    main()
