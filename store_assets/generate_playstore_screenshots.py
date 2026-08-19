import subprocess
import os

OUTPUT_DIR = "store_assets/screenshots"
os.makedirs(OUTPUT_DIR, exist_ok=True)

SCREENSHOTS = [
    {
        "id": "1_instant_lock",
        "badge": "⚡ 0ms ZERO-DELAY ENGINE",
        "badge_color": "#3B82F6",
        "title": "0ms Instant App Lock",
        "subtitle": "Blocks WhatsApp & Banking apps with zero screen glimpse",
        "highlights": ["⚡ Zero Latency", "🛡️ Accessibility Layer", "🚫 No Screen Flash"],
        "screen_title": "WhatsApp is Locked",
        "screen_type": "lock_screen",
        "accent": "#3B82F6",
    },
    {
        "id": "2_intruder_selfie",
        "badge": "📸 SILENT FRONT-CAMERA DETECTOR",
        "badge_color": "#EF4444",
        "title": "Silent Intruder Selfie",
        "subtitle": "Silently captures snooper photos on incorrect passcode",
        "highlights": ["📸 Zero Shutter Sound", "🔇 No Screen Flash", "⚡ Instant Capture"],
        "screen_title": "Intruder Alert Triggered",
        "screen_type": "intruder_capture",
        "accent": "#EF4444",
    },
    {
        "id": "3_biometric_vault",
        "badge": "👁️ TAP TO REVEAL VAULT",
        "badge_color": "#06B6D4",
        "title": "Biometric Shield Vault",
        "subtitle": "Intruder evidence stays frosted until fingerprint unlock",
        "highlights": ["🔒 Frosted Previews", "👆 Fingerprint / Face ID", "🛡️ 100% On-Device"],
        "screen_title": "Intruder Evidence Logs",
        "screen_type": "vault_shield",
        "accent": "#06B6D4",
    },
    {
        "id": "4_hardware_encryption",
        "badge": "🔐 HARDWARE KEYSTORE AES-256",
        "badge_color": "#10B981",
        "title": "Military AES-256 GCM",
        "subtitle": "Hardware-backed Android KeyStore encryption on disk",
        "highlights": ["🛡️ AES-256 GCM", "🔑 Android KeyStore", "☁️ 0% Cloud Upload"],
        "screen_title": "Hardware Security Engine",
        "screen_type": "encryption_hub",
        "accent": "#10B981",
    },
    {
        "id": "5_lock_modes",
        "badge": "🔢 MULTIPLE PASSCODE STYLES",
        "badge_color": "#8B5CF6",
        "title": "PIN, Pattern & Password",
        "subtitle": "Flexible locking options with tactile feedback & biometrics",
        "highlights": ["🔢 4/6-Digit PIN", "✨ 3x3 Smooth Pattern", "🔤 Alphanumeric"],
        "screen_title": "Choose Your Lock Style",
        "screen_type": "lock_modes",
        "accent": "#8B5CF6",
    },
    {
        "id": "6_double_lock_advisor",
        "badge": "🔄 DUAL-LOCK LOOP PREVENTER",
        "badge_color": "#F59E0B",
        "title": "Smart Double-Lock Advisor",
        "subtitle": "Eliminates frustrating double-auth on banking & chats",
        "highlights": ["🔄 Loop Prevention", "🏦 Banking Optimized", "⚡ Smart Delay"],
        "screen_title": "Double-Lock Advisor",
        "screen_type": "double_lock",
        "accent": "#F59E0B",
    },
    {
        "id": "7_forensic_shredder",
        "badge": "🗑️ 3-PASS PERMANENT SHREDDER",
        "badge_color": "#EC4899",
        "title": "3-Pass Forensic Shredder",
        "subtitle": "Overwrites storage sectors with random noise & zeros",
        "highlights": ["🗑️ 3-Pass DoD Wipe", "🚫 Zero Data Recovery", "🔒 Permanent Purge"],
        "screen_title": "Secure File Shredder",
        "screen_type": "shredder",
        "accent": "#EC4899",
    },
    {
        "id": "8_security_hub",
        "badge": "💎 COMPLETE COMMAND CENTER",
        "badge_color": "#6366F1",
        "title": "Privacy Command Center",
        "subtitle": "Per-app re-lock timers, camouflage & dark Material 3 UI",
        "highlights": ["⏱️ Re-Lock Timers", "🛡️ All Apps Supported", "🌙 Fluid M3 Theme"],
        "screen_title": "App Locker Pro",
        "screen_type": "security_hub",
        "accent": "#6366F1",
    },
]

def generate_screen_content(screen_type, accent):
    if screen_type == "lock_screen":
        return f'''
        <!-- App Logo Header -->
        <g transform="translate(180, 100)">
            <circle cx="50" cy="50" r="44" fill="#25D366" />
            <path d="M 32,32 C 32,45 42,55 55,55 C 57,55 60,54 62,53 L 68,55 L 66,49 C 67,47 68,45 68,42 C 68,29 58,19 45,19 C 32,19 32,32 32,32 Z" fill="#FFFFFF" transform="translate(-10, -5) scale(1.2)" />
        </g>
        <text x="230" y="230" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">WhatsApp</text>
        <text x="230" y="260" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="18">Enter 4-Digit PIN to Access</text>

        <!-- PIN Dots -->
        <g transform="translate(120, 290)">
            <circle cx="40" cy="20" r="12" fill="{accent}" />
            <circle cx="90" cy="20" r="12" fill="{accent}" />
            <circle cx="140" cy="20" r="12" fill="{accent}" />
            <circle cx="190" cy="20" r="12" fill="#334155" stroke="#475569" stroke-width="2" />
        </g>

        <!-- Keypad -->
        <g transform="translate(50, 360)">
            <!-- Row 1 -->
            <rect x="20" y="0" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="65" y="46" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">1</text>
            <rect x="135" y="0" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="180" y="46" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">2</text>
            <rect x="250" y="0" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="295" y="46" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">3</text>

            <!-- Row 2 -->
            <rect x="20" y="90" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="65" y="136" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">4</text>
            <rect x="135" y="90" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="180" y="136" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">5</text>
            <rect x="250" y="90" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="295" y="136" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">6</text>

            <!-- Row 3 -->
            <rect x="20" y="180" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="65" y="226" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">7</text>
            <rect x="135" y="180" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="180" y="226" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">8</text>
            <rect x="250" y="180" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="295" y="226" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">9</text>

            <!-- Row 4 -->
            <rect x="20" y="270" width="90" height="75" rx="20" fill="#0F172A" />
            <circle cx="65" cy="308" r="14" fill="none" stroke="{accent}" stroke-width="3" />
            <path d="M 58,308 Q 65,300 72,308" fill="none" stroke="{accent}" stroke-width="2.5" />
            <rect x="135" y="270" width="90" height="75" rx="20" fill="#1E293B" />
            <text x="180" y="316" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="28" font-weight="bold">0</text>
            <rect x="250" y="270" width="90" height="75" rx="20" fill="#0F172A" />
            <text x="295" y="316" text-anchor="middle" fill="#EF4444" font-family="sans-serif" font-size="24">⌫</text>
        </g>
        
        <!-- Bottom Protection Tag -->
        <rect x="60" y="740" width="340" height="42" rx="12" fill="rgba(59, 130, 246, 0.15)" stroke="rgba(59, 130, 246, 0.3)" stroke-width="1.5" />
        <text x="230" y="767" text-anchor="middle" fill="#60A5FA" font-family="sans-serif" font-size="14" font-weight="bold">⚡ 0ms Instant Interception Active</text>
        '''
    elif screen_type == "intruder_capture":
        return f'''
        <!-- Warning Card -->
        <rect x="30" y="70" width="400" height="100" rx="18" fill="rgba(239, 68, 68, 0.15)" stroke="#EF4444" stroke-width="2" />
        <circle cx="75" cy="120" r="26" fill="#EF4444" />
        <text x="75" y="129" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="24" font-weight="bold">!</text>
        <text x="120" y="110" fill="#FCA5A5" font-family="sans-serif" font-size="18" font-weight="bold">INTRUDER DETECTED</text>
        <text x="120" y="135" fill="#94A3B8" font-family="sans-serif" font-size="14">3 Failed unlock attempts on Gallery</text>

        <!-- Viewfinder Box -->
        <g transform="translate(60, 200)">
            <rect x="0" y="0" width="340" height="360" rx="24" fill="#0F172A" stroke="#334155" stroke-width="2" />
            <!-- Camera Silhouette / Crosshair -->
            <circle cx="170" cy="150" r="60" fill="#1E293B" stroke="#EF4444" stroke-width="3" stroke-dasharray="8,6" />
            <circle cx="170" cy="130" r="24" fill="#475569" />
            <path d="M 130,190 C 130,165 210,165 210,190 Z" fill="#475569" />
            
            <!-- Snapshot Tag -->
            <rect x="70" y="240" width="200" height="34" rx="17" fill="#EF4444" />
            <text x="170" y="262" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="13" font-weight="bold">🔴 SILENT CAPTURE SAVED</text>

            <!-- Metadata Box -->
            <text x="170" y="305" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="15" font-weight="bold">Encrypted with AES-256 GCM</text>
            <text x="170" y="328" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="13">Aug 18, 2026 • 09:41:22 PM</text>
        </g>

        <!-- Security Rule Status -->
        <rect x="30" y="600" width="400" height="150" rx="20" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
        <text x="50" y="635" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Intruder Trigger Policies</text>
        
        <rect x="50" y="655" width="360" height="36" rx="8" fill="#1E293B" />
        <text x="65" y="678" fill="#94A3B8" font-family="sans-serif" font-size="13">Capture after: <tspan fill="#EF4444" font-weight="bold">1 Incorrect Attempt</tspan></text>

        <rect x="50" y="700" width="360" height="36" rx="8" fill="#1E293B" />
        <text x="65" y="723" fill="#94A3B8" font-family="sans-serif" font-size="13">Silent Mode: <tspan fill="#10B981" font-weight="bold">No Flash • No Shutter</tspan></text>
        '''
    elif screen_type == "vault_shield":
        return f'''
        <!-- Top Bar with Shield -->
        <rect x="25" y="60" width="410" height="70" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
        <text x="45" y="102" fill="#F8FAFC" font-family="sans-serif" font-size="18" font-weight="bold">Intruder Vault (4 Logs)</text>
        <rect x="305" y="78" width="115" height="34" rx="8" fill="rgba(6, 182, 212, 0.15)" />
        <text x="362" y="100" text-anchor="middle" fill="#06B6D4" font-family="sans-serif" font-size="12" font-weight="bold">🔒 SHIELDED</text>

        <!-- Log Item 1: Frosted Shielded Preview -->
        <g transform="translate(25, 150)">
            <rect x="0" y="0" width="410" height="180" rx="20" fill="#131B2E" stroke="{accent}" stroke-width="2" />
            <!-- Frosted Glass Image Area -->
            <rect x="15" y="15" width="150" height="150" rx="14" fill="#1E293B" />
            <circle cx="90" cy="75" r="30" fill="rgba(6, 182, 212, 0.2)" stroke="{accent}" stroke-width="2" />
            <path d="M 80,75 Q 90,65 100,75 M 84,80 Q 90,72 96,80 M 88,85 Q 90,80 92,85" fill="none" stroke="{accent}" stroke-width="3" stroke-linecap="round" />
            <text x="90" y="125" text-anchor="middle" fill="#06B6D4" font-family="sans-serif" font-size="11" font-weight="bold">TAP TO UNBLUR</text>

            <!-- Info Side -->
            <text x="180" y="45" fill="#F8FAFC" font-family="sans-serif" font-size="17" font-weight="bold">WhatsApp</text>
            <text x="180" y="70" fill="#EF4444" font-family="sans-serif" font-size="13" font-weight="bold">🔴 2 Failed PIN Tries</text>
            <text x="180" y="95" fill="#94A3B8" font-family="sans-serif" font-size="12">Today, 09:41 PM</text>
            <rect x="180" y="115" width="120" height="30" rx="8" fill="#1E293B" />
            <text x="240" y="135" text-anchor="middle" fill="#64748B" font-family="sans-serif" font-size="11">AES-256 .enc</text>
        </g>

        <!-- Log Item 2 -->
        <g transform="translate(25, 350)">
            <rect x="0" y="0" width="410" height="180" rx="20" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <!-- Frosted Glass Image Area -->
            <rect x="15" y="15" width="150" height="150" rx="14" fill="#1E293B" />
            <circle cx="90" cy="75" r="30" fill="rgba(6, 182, 212, 0.2)" stroke="{accent}" stroke-width="2" />
            <path d="M 80,75 Q 90,65 100,75 M 84,80 Q 90,72 96,80" fill="none" stroke="{accent}" stroke-width="3" stroke-linecap="round" />
            <text x="90" y="125" text-anchor="middle" fill="#06B6D4" font-family="sans-serif" font-size="11" font-weight="bold">TAP TO UNBLUR</text>

            <!-- Info Side -->
            <text x="180" y="45" fill="#F8FAFC" font-family="sans-serif" font-size="17" font-weight="bold">Google Photos</text>
            <text x="180" y="70" fill="#EF4444" font-family="sans-serif" font-size="13" font-weight="bold">🔴 Pattern Mismatch</text>
            <text x="180" y="95" fill="#94A3B8" font-family="sans-serif" font-size="12">Yesterday, 04:15 PM</text>
            <rect x="180" y="115" width="120" height="30" rx="8" fill="#1E293B" />
            <text x="240" y="135" text-anchor="middle" fill="#64748B" font-family="sans-serif" font-size="11">AES-256 .enc</text>
        </g>

        <!-- Biometric Unlock Button -->
        <g transform="translate(50, 560)">
            <rect x="0" y="0" width="360" height="60" rx="16" fill="linear-gradient(135deg, #0891B2, #2563EB)" />
            <text x="180" y="37" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="16" font-weight="bold">👆 Unlock Vault with Biometrics</text>
        </g>
        '''
    elif screen_type == "encryption_hub":
        return f'''
        <!-- Security Shield Hero -->
        <g transform="translate(180, 80)">
            <circle cx="50" cy="50" r="50" fill="rgba(16, 185, 129, 0.15)" stroke="{accent}" stroke-width="3" />
            <path d="M 50,25 L 75,38 L 75,65 C 75,80 50,90 50,90 C 50,90 25,80 25,65 L 25,38 Z" fill="{accent}" />
            <path d="M 45,55 L 50,60 L 60,48" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-linecap="round" />
        </g>
        <text x="230" y="210" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="22" font-weight="bold">Hardware Cryptography Active</text>
        <text x="230" y="235" text-anchor="middle" fill="#10B981" font-family="sans-serif" font-size="14" font-weight="bold">Android KeyStore • AES-256 GCM</text>

        <!-- Specs Cards -->
        <g transform="translate(30, 260)">
            <rect x="0" y="0" width="400" height="100" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="45" cy="50" r="22" fill="#1E293B" stroke="{accent}" stroke-width="2" />
            <text x="45" y="56" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="16" font-weight="bold">🔑</text>
            <text x="85" y="42" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Hardware Keystore Keys</text>
            <text x="85" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">Symmetric keys never leave Secure Element</text>
            <text x="85" y="85" fill="#10B981" font-family="sans-serif" font-size="12" font-weight="bold">✓ TEE / StrongBox Backed</text>
        </g>

        <g transform="translate(30, 380)">
            <rect x="0" y="0" width="400" height="100" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="45" cy="50" r="22" fill="#1E293B" stroke="{accent}" stroke-width="2" />
            <text x="45" y="56" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="16" font-weight="bold">📁</text>
            <text x="85" y="42" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">In-Memory Encryption</text>
            <text x="85" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">Raw photos never touch unencrypted disk</text>
            <text x="85" y="85" fill="#10B981" font-family="sans-serif" font-size="12" font-weight="bold">✓ 0 Bytes Plaintext Leakage</text>
        </g>

        <g transform="translate(30, 500)">
            <rect x="0" y="0" width="400" height="100" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="45" cy="50" r="22" fill="#1E293B" stroke="{accent}" stroke-width="2" />
            <text x="45" y="56" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="16" font-weight="bold">☁️</text>
            <text x="85" y="42" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Zero Cloud Transmission</text>
            <text x="85" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">100% On-device sandboxed storage</text>
            <text x="85" y="85" fill="#10B981" font-family="sans-serif" font-size="12" font-weight="bold">✓ Offline Air-Gapped Privacy</text>
        </g>
        '''
    elif screen_type == "lock_modes":
        return f'''
        <!-- Header -->
        <text x="230" y="90" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="22" font-weight="bold">Select Master Lock Type</text>
        <text x="230" y="115" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="14">Customizable security for every app</text>

        <!-- Option 1: PIN Card -->
        <g transform="translate(30, 140)">
            <rect x="0" y="0" width="400" height="90" rx="16" fill="#131B2E" stroke="{accent}" stroke-width="2" />
            <rect x="20" y="20" width="50" height="50" rx="12" fill="rgba(139, 92, 246, 0.2)" />
            <text x="45" y="52" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="22" font-weight="bold">🔢</text>
            <text x="85" y="42" fill="#F8FAFC" font-family="sans-serif" font-size="17" font-weight="bold">4 or 6-Digit PIN Code</text>
            <text x="85" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">Tactile responsive number pad</text>
            <circle cx="360" cy="45" r="14" fill="{accent}" />
            <path d="M 354,45 L 358,49 L 366,41" fill="none" stroke="#FFFFFF" stroke-width="2.5" />
        </g>

        <!-- Option 2: Pattern Grid Showcase -->
        <g transform="translate(30, 250)">
            <rect x="0" y="0" width="400" height="230" rx="20" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <text x="25" y="35" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">✨ 3x3 Pattern Matrix</text>
            
            <!-- Pattern 3x3 Dots -->
            <g transform="translate(130, 60)">
                <!-- Connected lines -->
                <path d="M 20,20 L 70,20 L 120,70 L 70,120" fill="none" stroke="{accent}" stroke-width="4" stroke-linecap="round" />
                
                <!-- Dot 1,1 -->
                <circle cx="20" cy="20" r="10" fill="{accent}" stroke="#C4B5FD" stroke-width="3" />
                <!-- Dot 1,2 -->
                <circle cx="70" cy="20" r="10" fill="{accent}" stroke="#C4B5FD" stroke-width="3" />
                <!-- Dot 1,3 -->
                <circle cx="120" cy="20" r="7" fill="#475569" />

                <!-- Dot 2,1 -->
                <circle cx="20" cy="70" r="7" fill="#475569" />
                <!-- Dot 2,2 -->
                <circle cx="70" cy="70" r="7" fill="#475569" />
                <!-- Dot 2,3 -->
                <circle cx="120" cy="70" r="10" fill="{accent}" stroke="#C4B5FD" stroke-width="3" />

                <!-- Dot 3,1 -->
                <circle cx="20" cy="120" r="7" fill="#475569" />
                <!-- Dot 3,2 -->
                <circle cx="70" cy="120" r="10" fill="{accent}" stroke="#C4B5FD" stroke-width="3" />
                <!-- Dot 3,3 -->
                <circle cx="120" cy="120" r="7" fill="#475569" />
            </g>
        </g>

        <!-- Option 3: Biometric Fingerprint -->
        <g transform="translate(30, 500)">
            <rect x="0" y="0" width="400" height="90" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <rect x="20" y="20" width="50" height="50" rx="12" fill="rgba(139, 92, 246, 0.2)" />
            <text x="45" y="52" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="22" font-weight="bold">👆</text>
            <text x="85" y="42" fill="#F8FAFC" font-family="sans-serif" font-size="17" font-weight="bold">Biometric Authentication</text>
            <text x="85" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">Instant Fingerprint & Face Unlock</text>
        </g>
        '''
    elif screen_type == "double_lock":
        return f'''
        <!-- Advisor Banner -->
        <rect x="25" y="60" width="410" height="110" rx="18" fill="rgba(245, 158, 11, 0.15)" stroke="{accent}" stroke-width="2" />
        <circle cx="65" cy="115" r="24" fill="{accent}" />
        <text x="65" y="123" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="22" font-weight="bold">💡</text>
        <text x="105" y="100" fill="#FCD34D" font-family="sans-serif" font-size="16" font-weight="bold">Smart Double-Lock Advisor</text>
        <text x="105" y="125" fill="#94A3B8" font-family="sans-serif" font-size="13">3 apps with native biometrics detected</text>
        <text x="105" y="145" fill="#FCD34D" font-family="sans-serif" font-size="12" font-weight="bold">✓ Loop prevention active</text>

        <!-- Detected App 1 -->
        <g transform="translate(25, 190)">
            <rect x="0" y="0" width="410" height="90" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="45" cy="45" r="22" fill="#25D366" />
            <text x="45" y="52" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="18">💬</text>
            <text x="80" y="38" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">WhatsApp</text>
            <text x="80" y="60" fill="#94A3B8" font-family="sans-serif" font-size="12">Has native Fingerprint Lock</text>
            <!-- Toggle Switch ON -->
            <rect x="330" y="30" width="55" height="30" rx="15" fill="{accent}" />
            <circle cx="368" cy="45" r="11" fill="#FFFFFF" />
        </g>

        <!-- Detected App 2 -->
        <g transform="translate(25, 300)">
            <rect x="0" y="0" width="410" height="90" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="45" cy="45" r="22" fill="#3B82F6" />
            <text x="45" y="52" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="18">🏦</text>
            <text x="80" y="38" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Google Wallet / Banking</text>
            <text x="80" y="60" fill="#94A3B8" font-family="sans-serif" font-size="12">Has biometric authentication</text>
            <!-- Toggle Switch ON -->
            <rect x="330" y="30" width="55" height="30" rx="15" fill="{accent}" />
            <circle cx="368" cy="45" r="11" fill="#FFFFFF" />
        </g>

        <!-- Smart Delay Slider Card -->
        <g transform="translate(25, 410)">
            <rect x="0" y="0" width="410" height="150" rx="20" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <text x="25" y="35" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Re-Lock Delay Grace Period</text>
            <text x="25" y="60" fill="#94A3B8" font-family="sans-serif" font-size="13">Avoid re-prompting if switching back within:</text>
            
            <rect x="25" y="85" width="80" height="38" rx="10" fill="#1E293B" />
            <text x="65" y="109" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="13">0s</text>

            <rect x="115" y="85" width="80" height="38" rx="10" fill="{accent}" />
            <text x="155" y="109" text-anchor="middle" fill="#000000" font-family="sans-serif" font-size="13" font-weight="bold">15s</text>

            <rect x="205" y="85" width="80" height="38" rx="10" fill="#1E293B" />
            <text x="245" y="109" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="13">30s</text>

            <rect x="295" y="85" width="85" height="38" rx="10" fill="#1E293B" />
            <text x="337" y="109" text-anchor="middle" fill="#94A3B8" font-family="sans-serif" font-size="13">1 min</text>
        </g>
        '''
    elif screen_type == "shredder":
        return f'''
        <!-- Shredder Hero -->
        <g transform="translate(180, 70)">
            <circle cx="50" cy="50" r="48" fill="rgba(236, 72, 153, 0.15)" stroke="{accent}" stroke-width="3" />
            <text x="50" y="60" text-anchor="middle" fill="{accent}" font-family="sans-serif" font-size="36">🗑️</text>
        </g>
        <text x="230" y="195" text-anchor="middle" fill="#F8FAFC" font-family="sans-serif" font-size="22" font-weight="bold">Forensic File Shredder</text>
        <text x="230" y="220" text-anchor="middle" fill="#EC4899" font-family="sans-serif" font-size="14" font-weight="bold">DoD 5220.22-M Overwrite Standard</text>

        <!-- Shredding Progress Card -->
        <g transform="translate(25, 250)">
            <rect x="0" y="0" width="410" height="310" rx="20" fill="#131B2E" stroke="{accent}" stroke-width="2" />
            
            <text x="25" y="40" fill="#F8FAFC" font-family="sans-serif" font-size="17" font-weight="bold">Shredding Intruder Snapshot</text>
            <text x="25" y="65" fill="#94A3B8" font-family="sans-serif" font-size="13">Target: <tspan fill="#F8FAFC" font-family="monospace">intruder_20260818.enc</tspan></text>

            <!-- Pass 1 -->
            <g transform="translate(25, 90)">
                <circle cx="12" cy="12" r="10" fill="#10B981" />
                <path d="M 8,12 L 11,15 L 17,9" fill="none" stroke="#FFFFFF" stroke-width="2" />
                <text x="32" y="17" fill="#F8FAFC" font-family="sans-serif" font-size="14">Pass 1: Cryptographic Random Bytes</text>
            </g>

            <!-- Pass 2 -->
            <g transform="translate(25, 130)">
                <circle cx="12" cy="12" r="10" fill="#10B981" />
                <path d="M 8,12 L 11,15 L 17,9" fill="none" stroke="#FFFFFF" stroke-width="2" />
                <text x="32" y="17" fill="#F8FAFC" font-family="sans-serif" font-size="14">Pass 2: Bitwise Inverse Mask (0xFF)</text>
            </g>

            <!-- Pass 3 (Active) -->
            <g transform="translate(25, 170)">
                <circle cx="12" cy="12" r="10" fill="{accent}" />
                <text x="32" y="17" fill="#EC4899" font-family="sans-serif" font-size="14" font-weight="bold">Pass 3: Cryptographic Zeros (0x00)</text>
            </g>

            <!-- Progress Bar -->
            <rect x="25" y="215" width="360" height="14" rx="7" fill="#1E293B" />
            <rect x="25" y="215" width="320" height="14" rx="7" fill="{accent}" />
            <text x="385" y="227" text-anchor="end" fill="{accent}" font-family="sans-serif" font-size="12" font-weight="bold">92%</text>

            <!-- Verification Badge -->
            <rect x="25" y="250" width="360" height="40" rx="10" fill="#1E293B" />
            <text x="205" y="275" text-anchor="middle" fill="#10B981" font-family="sans-serif" font-size="13" font-weight="bold">🛡️ Zero Disk Recovery Guaranteed</text>
        </g>
        '''
    elif screen_type == "security_hub":
        return f'''
        <!-- Main Hub Header -->
        <g transform="translate(25, 60)">
            <rect x="0" y="0" width="410" height="90" rx="20" fill="linear-gradient(135deg, #1E1B4B, #312E81)" stroke="{accent}" stroke-width="2" />
            <text x="25" y="38" fill="#F8FAFC" font-family="sans-serif" font-size="18" font-weight="bold">Active Protection</text>
            <text x="25" y="62" fill="#A5B4FC" font-family="sans-serif" font-size="13">18 Apps Locked • Zero Lag Mode</text>
            <!-- Big ON Toggle -->
            <rect x="320" y="25" width="65" height="36" rx="18" fill="#10B981" />
            <circle cx="363" cy="43" r="14" fill="#FFFFFF" />
        </g>

        <!-- Search Bar -->
        <g transform="translate(25, 165)">
            <rect x="0" y="0" width="410" height="46" rx="14" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <text x="20" y="28" fill="#64748B" font-family="sans-serif" font-size="16">🔍</text>
            <text x="50" y="28" fill="#64748B" font-family="sans-serif" font-size="14">Search apps to lock...</text>
        </g>

        <!-- App 1: WhatsApp -->
        <g transform="translate(25, 225)">
            <rect x="0" y="0" width="410" height="75" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="40" cy="37" r="20" fill="#25D366" />
            <text x="75" y="34" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">WhatsApp</text>
            <text x="75" y="54" fill="#94A3B8" font-family="sans-serif" font-size="12">Social • Immediate Lock</text>
            <circle cx="365" cy="37" r="16" fill="{accent}" />
            <text x="365" y="43" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="14">🔒</text>
        </g>

        <!-- App 2: Google Photos -->
        <g transform="translate(25, 310)">
            <rect x="0" y="0" width="410" height="75" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="40" cy="37" r="20" fill="#EA4335" />
            <text x="75" y="34" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Google Photos</text>
            <text x="75" y="54" fill="#94A3B8" font-family="sans-serif" font-size="12">Gallery • Intruder Snapshot ON</text>
            <circle cx="365" cy="37" r="16" fill="{accent}" />
            <text x="365" y="43" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="14">🔒</text>
        </g>

        <!-- App 3: Settings -->
        <g transform="translate(25, 395)">
            <rect x="0" y="0" width="410" height="75" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="40" cy="37" r="20" fill="#475569" />
            <text x="75" y="34" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Android System Settings</text>
            <text x="75" y="54" fill="#94A3B8" font-family="sans-serif" font-size="12">Prevents Unauthorized Uninstall</text>
            <circle cx="365" cy="37" r="16" fill="{accent}" />
            <text x="365" y="43" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="14">🔒</text>
        </g>

        <!-- App 4: Google Play Store -->
        <g transform="translate(25, 480)">
            <rect x="0" y="0" width="410" height="75" rx="16" fill="#131B2E" stroke="#1E293B" stroke-width="1.5" />
            <circle cx="40" cy="37" r="20" fill="#3B82F6" />
            <text x="75" y="34" fill="#F8FAFC" font-family="sans-serif" font-size="16" font-weight="bold">Google Play Store</text>
            <text x="75" y="54" fill="#94A3B8" font-family="sans-serif" font-size="12">Prevents App Installations</text>
            <circle cx="365" cy="37" r="16" fill="{accent}" />
            <text x="365" y="43" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="14">🔒</text>
        </g>
        '''
    return ""

def build_screenshot_svg(item):
    badge = item["badge"]
    badge_color = item["badge_color"]
    title = item["title"]
    subtitle = item["subtitle"]
    highlights = item["highlights"]
    accent = item["accent"]
    screen_type = item["screen_type"]
    
    screen_content = generate_screen_content(screen_type, accent)
    
    h_tags = ""
    for i, hl in enumerate(highlights):
        x_offset = 120 + i * 290
        h_tags += f'''
        <g transform="translate({x_offset}, 440)">
            <rect x="0" y="0" width="270" height="48" rx="24" fill="rgba(30, 41, 59, 0.9)" stroke="{accent}" stroke-width="1.5" />
            <text x="135" y="30" text-anchor="middle" fill="#F8FAFC" font-family="'Plus Jakarta Sans', sans-serif" font-size="17" font-weight="bold">{hl}</text>
        </g>
        '''

    svg = f'''<?xml version="1.0" encoding="UTF-8"?>
<svg width="1080" height="1920" viewBox="0 0 1080 1920" xmlns="http://www.w3.org/2000/svg">
    <defs>
        <!-- Background Gradient -->
        <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#080C14"/>
            <stop offset="50%" stop-color="#0F172A"/>
            <stop offset="100%" stop-color="#020617"/>
        </linearGradient>

        <!-- Ambient Top Glow -->
        <radialGradient id="topGlow" cx="50%" cy="15%" r="60%">
            <stop offset="0%" stop-color="{accent}" stop-opacity="0.35"/>
            <stop offset="100%" stop-color="{accent}" stop-opacity="0.0"/>
        </radialGradient>

        <!-- Phone Frame Gradient -->
        <linearGradient id="phoneBorder" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#475569"/>
            <stop offset="50%" stop-color="#1E293B"/>
            <stop offset="100%" stop-color="#64748B"/>
        </linearGradient>

        <!-- Phone Screen Inner Gradient -->
        <linearGradient id="phoneInner" x1="0%" y1="0%" x2="0%" y2="100%">
            <stop offset="0%" stop-color="#0B0F19"/>
            <stop offset="100%" stop-color="#070A10"/>
        </linearGradient>

        <!-- Drop Shadow Filter -->
        <filter id="phoneShadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="25" stdDeviation="40" flood-color="#000000" flood-opacity="0.75" />
            <feDropShadow dx="0" dy="0" stdDeviation="30" flood-color="{accent}" flood-opacity="0.25" />
        </filter>
    </defs>

    <!-- Canvas Background -->
    <rect width="1080" height="1920" fill="url(#bgGrad)" />
    <rect width="1080" height="1920" fill="url(#topGlow)" />

    <!-- TOP HEADER SECTION -->
    <!-- Feature Pill Badge -->
    <g transform="translate(540, 140)">
        <rect x="-240" y="-24" width="480" height="48" rx="24" fill="rgba(15, 23, 42, 0.85)" stroke="{badge_color}" stroke-width="2" />
        <text x="0" y="7" text-anchor="middle" fill="{badge_color}" font-family="'Plus Jakarta Sans', sans-serif" font-size="20" font-weight="bold" letter-spacing="1.5">{badge}</text>
    </g>

    <!-- Main Title -->
    <text x="540" y="270" text-anchor="middle" fill="#FFFFFF" font-family="'Plus Jakarta Sans', sans-serif" font-size="64" font-weight="800" letter-spacing="-1">{title}</text>

    <!-- Subtitle -->
    <text x="540" y="340" text-anchor="middle" fill="#94A3B8" font-family="'Plus Jakarta Sans', sans-serif" font-size="28" font-weight="500">{subtitle}</text>

    <!-- Feature Highlight Pills -->
    {h_tags}

    <!-- 3D SMARTPHONE DEVICE MOCKUP -->
    <g transform="translate(310, 520)" filter="url(#phoneShadow)">
        <!-- Outer Chassis -->
        <rect x="0" y="0" width="460" height="1350" rx="55" fill="url(#phoneBorder)" stroke="#334155" stroke-width="4" />
        
        <!-- Screen Bezel -->
        <rect x="10" y="10" width="440" height="1330" rx="46" fill="#000000" />

        <!-- Phone Display Screen -->
        <rect x="16" y="16" width="428" height="1318" rx="40" fill="url(#phoneInner)" />

        <!-- Camera Punch Hole Cutout -->
        <circle cx="230" cy="40" r="9" fill="#000000" />
        <circle cx="230" cy="40" r="5" fill="#0F172A" stroke="#1E293B" stroke-width="1.5" />

        <!-- Android Status Bar -->
        <text x="50" y="46" fill="#94A3B8" font-family="sans-serif" font-size="14" font-weight="bold">9:41</text>
        <g transform="translate(370, 36)">
            <!-- 5G & Battery -->
            <text x="0" y="10" fill="#94A3B8" font-family="sans-serif" font-size="12" font-weight="bold">5G</text>
            <rect x="22" y="0" width="22" height="12" rx="3" fill="none" stroke="#94A3B8" stroke-width="1.5" />
            <rect x="24" y="2" width="16" height="8" rx="2" fill="#10B981" />
        </g>

        <!-- UI SCREEN CONTENT INSIDE PHONE -->
        <g transform="translate(0, 50)">
            {screen_content}
        </g>

        <!-- Android Home Navigation Pill Indicator -->
        <rect x="170" y="1300" width="120" height="5" rx="2.5" fill="#64748B" />
    </g>

</svg>
'''
    return svg

def main():
    print("Generating 8 Google Play Store Phone Screenshots...")
    for idx, item in enumerate(SCREENSHOTS, 1):
        svg_filename = f"{OUTPUT_DIR}/screenshot_{item['id']}.svg"
        png_filename = f"{OUTPUT_DIR}/screenshot_{item['id']}.png"
        
        svg_content = build_screenshot_svg(item)
        with open(svg_filename, "w", encoding="utf-8") as f:
            f.write(svg_content)
        
        # Convert SVG to 1080x1920 PNG using ImageMagick convert
        cmd = ["convert", "-background", "none", svg_filename, png_filename]
        res = subprocess.run(cmd, capture_output=True, text=True)
        if res.returncode == 0:
            print(f"[{idx}/8] Created: {png_filename} (1080x1920)")
        else:
            print(f"[{idx}/8] Error converting {svg_filename}: {res.stderr}")

    print("\nAll 8 Play Store phone screenshots generated successfully in store_assets/screenshots/")

if __name__ == "__main__":
    main()
