import json
import datetime
import random
import os
import sqlite3
import hmac
import hashlib

class LocalVectorMemory:
    """
    Simulates a high-performance vector memory engine using an SQLite database with TF-IDF/keyword overlap
    for semantic indexing and 5-year perpetual recall to prevent any forgetting.
    """
    def __init__(self, db_path="python_core/local_memory.db"):
        self.db_path = db_path
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
        self._init_db()

    def _init_db(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            # Perpetual Memory Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS memories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    content TEXT NOT NULL,
                    tags TEXT,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                )
            """)
            # CRM Leads Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS crm_leads (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    company TEXT,
                    status TEXT DEFAULT 'Lead', -- Lead, Discovery, Negotiation, Payment, Success
                    budget REAL DEFAULT 0.0,
                    notes TEXT,
                    last_interaction TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)
            # UPI Invoices Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS invoices (
                    invoice_id TEXT PRIMARY KEY,
                    client_name TEXT,
                    amount REAL,
                    status TEXT DEFAULT 'Pending', -- Pending, Paid, Refunded
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """)
            # Reinforcement Learning / Tone Weights Table
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS rl_state (
                    key TEXT PRIMARY KEY,
                    value REAL
                )
            """)
            cursor.execute("INSERT OR IGNORE INTO rl_state (key, value) VALUES ('affection_weight', 1.0)")
            cursor.execute("INSERT OR IGNORE INTO rl_state (key, value) VALUES ('sales_conversion_weight', 1.0)")
            conn.commit()

    def add_memory(self, content, tags="general"):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO memories (content, tags) VALUES (?, ?)", (content, tags))
            conn.commit()

    def query_memory(self, query, limit=3):
        """Keyword-overlap semantic search simulation for 5-year contextual recall"""
        words = [w.lower() for w in query.split() if len(w) > 3]
        if not words:
            # Fallback to latest memories
            with sqlite3.connect(self.db_path) as conn:
                cursor = conn.cursor()
                cursor.execute("SELECT content FROM memories ORDER BY timestamp DESC LIMIT ?", (limit,))
                return [row[0] for row in cursor.fetchall()]

        # Build basic keyword matching query
        like_clauses = " OR ".join(["content LIKE ?" for _ in words])
        sql = f"SELECT content, timestamp FROM memories WHERE {like_clauses} ORDER BY timestamp DESC LIMIT ?"
        params = [f"%{w}%" for w in words] + [limit]

        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute(sql, params)
            results = [row[0] for row in cursor.fetchall()]
            
        if not results:
            # Fallback to general
            return self.query_memory("", limit)
        return results

    # CRM Operations
    def add_lead(self, name, email="", phone="", company="", status="Lead", budget=0.0, notes=""):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT INTO crm_leads (name, email, phone, company, status, budget, notes)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """, (name, email, phone, company, status, budget, notes))
            conn.commit()
            return cursor.lastrowid

    def update_lead_status(self, lead_id, status):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("UPDATE crm_leads SET status = ?, last_interaction = CURRENT_TIMESTAMP WHERE id = ?", (status, lead_id))
            conn.commit()

    def get_leads(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM crm_leads ORDER BY last_interaction DESC")
            columns = [col[0] for col in cursor.description]
            return [dict(zip(columns, row)) for row in cursor.fetchall()]

    # Invoice Operations
    def create_invoice(self, invoice_id, client_name, amount):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT INTO invoices (invoice_id, client_name, amount) VALUES (?, ?, ?)", (invoice_id, client_name, amount))
            conn.commit()

    def mark_invoice_paid(self, invoice_id):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("UPDATE invoices SET status = 'Paid' WHERE invoice_id = ?", (invoice_id,))
            conn.commit()

    def get_invoices(self):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM invoices ORDER BY created_at DESC")
            columns = [col[0] for col in cursor.description]
            return [dict(zip(columns, row)) for row in cursor.fetchall()]

    # Reinforcement Learning States
    def get_rl_weight(self, key):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT value FROM rl_state WHERE key = ?", (key,))
            row = cursor.fetchone()
            return row[0] if row else 1.0

    def set_rl_weight(self, key, value):
        with sqlite3.connect(self.db_path) as conn:
            cursor = conn.cursor()
            cursor.execute("INSERT OR REPLACE INTO rl_state (key, value) VALUES (?, ?)", (key, value))
            conn.commit()


class AssistantEngine:
    def __init__(self):
        # Initialize Vector database with SQLite for perpetual storage
        self.db = LocalVectorMemory()
        
        # State & Settings
        self.settings = {
            "assistant_name": "Snaper",
            "owner_name": "Sanjiv Sir",
            "owner_title": "Sanjiv Sir",
            "active_mode": "IT & Business Automation",
            "language_code": "en",
            "auto_start_boot": True,
            "privacy_wall_enabled": True,
            "low_network_mode": False,
            "upi_id": "sanjiv@okaxis",
            "monthly_target": 500000.0,
            "current_revenue": 120000.0,
            "company_name": "Snaper Technology Pvt Ltd",
            "custom_platforms": []
        }

        # Setup standard memories once
        if not self.db.query_memory("Sanjiv Sir"):
            self.db.add_memory("Owner prefers to be addressed as 'Sanjiv Sir'.")
            self.db.add_memory("Favorite greeting is 'Radhe Radhe'.")
            self.db.add_memory("Sanjiv runs an IT & AI Software Development Agency.")

        # Real-time state
        self.already_greeted_contacts = set()
        self.affection_level = 100
        self.is_pouting = False
        self.is_child_detected = False
        self.last_interaction_time = datetime.datetime.now()
        self.exam_schedules = {
            "Gopal": {"subject": "Mathematics", "date": (datetime.datetime.now() + datetime.timedelta(days=1)).strftime("%Y-%m-%d")}
        }
        self.iot_devices = [
            {"name": "Living Room AC", "type": "AC", "state": "OFF", "temp": 24, "protocol": "Matter"},
            {"name": "Study Lamp", "type": "Light", "state": "OFF", "protocol": "Zigbee"},
            {"name": "Main Door Lock", "type": "Lock", "state": "LOCKED", "protocol": "Z-Wave"},
            {"name": "Balcony Camera", "type": "Camera", "state": "STREAMING", "protocol": "Home Assistant"}
        ]

        # Dynamic family profiles discovered autonomously
        self.family_profiles = {
            "Mummy Ji": {"relationship": "Mother", "voice_profile": "Mother_Voice_Signature", "face_profile": "Mother_Face_Signature"},
            "Roshani": {"relationship": "Sister", "voice_profile": "Roshani_Voice_Signature", "face_profile": "Roshani_Face_Signature"}
        }

        # Local secure biometrics store for female/family privacy guarantee
        self.female_signatures = {
            "Roshani": {"face": "verified_roshani_face_hash_sha256", "voice": "verified_roshani_voice_hash_sha256"}
        }

        # MODULE 29, 30 & 31 security state
        self.security_logs = [
            {"timestamp": str(datetime.datetime.now() - datetime.timedelta(hours=1)), "event": "System initialized securely.", "severity": "INFO"}
        ]
        self.security_status = {
            "device_locked": False,
            "siren_active": False,
            "cctv_blocked_and_trapped": False,
            "police_notified": False,
            "engine_immobilized": False,
            "gyro_vision_active": False,
            "backup_completed": False,
            "last_captured_photo": None,
            "last_captured_timestamp": None,
            "vandalism_warning_played": False,
            "favorite_contacts_sos_sent": False
        }

    def dispatch(self, request):
        """Unified JSON router for cross-platform hardware controls and core AI operations"""
        action = request.get("action")
        payload = request.get("payload", {})

        # Bandwidth decompression / low-network mode booster
        if self.settings.get("low_network_mode") or payload.get("compress"):
            payload = self._decompress_payload(payload)

        try:
            if action == "ping":
                return {"status": "success", "response": "pong"}

            elif action == "trigger_phone_finder":
                alert_text = "संजीव जी, मैं यहाँ हूँ! (I am right here under the pillow/blanket)!"
                return {
                    "status": "success",
                    "response": alert_text,
                    "volume": "100%",
                    "chime_active": True,
                    "flashlight_blink_active": True,
                    "vibration_active": True,
                    "message": "Persistent acoustic listener matched trigger word. Overriding silent/DND and firing high-volume alert."
                }

            elif action == "detect_environment_sensing":
                light_lux = payload.get("light_lux", 2.0)
                proximity_near = payload.get("proximity_near", True)
                orientation = payload.get("orientation", "Face Down")
                if proximity_near and light_lux < 5.0:
                    environment = "Inside Blanket / Cushion"
                    details = "Pitch darkness with proximity obstruction detected."
                    hindi_msg = "संजीव जी, मैं यहाँ हूँ! मैं कम्बल या कुशन के नीचे दबी हुई हूँ!"
                elif light_lux < 15.0:
                    environment = "Under Pillow / Rack"
                    details = "Partial light obstruction detected."
                    hindi_msg = "संजीव जी, मैं यहाँ हूँ! मैं तकिये के नीचे या रैक के अंदर हूँ!"
                else:
                    environment = "Open Workstation / Desk"
                    details = "Normal ambient light detected."
                    hindi_msg = "संजीव सर, मैं आपके स्टडी टेबल पर खुली जगह में हूँ।"
                return {
                    "status": "success",
                    "environment": environment,
                    "details": details,
                    "hindi_alert": hindi_msg,
                    "orientation": orientation,
                    "flashlight_synergy": True,
                    "chime_synergy": True
                }

            elif action == "process_offline_command":
                is_offline = payload.get("is_offline", True)
                prompt = payload.get("prompt", "")
                cloud_only_keywords = ["send email", "post on instagram", "webhook", "api check", "cloud sync", "social media", "tweet"]
                is_cloud_only = any(kw in prompt.lower() for kw in cloud_only_keywords)
                if is_offline:
                    if is_cloud_only:
                        response_text = "संजीव सर, मैं अभी ऑफलाइन / फ्लाइट मोड में हूँ। यह क्लाउड-ओनली एक्शन (जैसे ईमेल या सोशल मीडिया पोस्ट) अभी पूरा नहीं किया जा सकता, लेकिन मैंने इसे लोकल स्टोर में सुरक्षित कर लिया है। नेटवर्क उपलब्ध होते ही यह स्वतः पूरा हो जाएगा।"
                    else:
                        response_text = "संजीव सर, मैं अभी लोकल-फर्स्ट ऑफलाइन इंजन पर पूरी सुरक्षा के साथ चल रही हूँ। आपके लोकल कंट्रोल, बायोमेट्रिक सुरक्षा, और फोन फाइंडर सुचारू रूप से कार्य कर रहे हैं।"
                    return {
                        "status": "success",
                        "response": response_text,
                        "offline_processing_active": True,
                        "local_biometrics_status": "Ready",
                        "local_speech_tts_active": True
                    }
                else:
                    return {
                        "status": "success",
                        "response": "Online hybrid mode active. Routing query through cloud-assisted models.",
                        "offline_processing_active": False
                    }

            elif action == "get_settings":
                return {"status": "success", "settings": self.settings}

            elif action == "update_settings":
                self.settings.update(payload)
                return {"status": "success", "settings": self.settings}

            elif action == "get_memories":
                mems = self.db.query_memory("", limit=50)
                return {"status": "success", "memories": mems}

            elif action == "add_memory":
                memory = payload.get("memory")
                if memory:
                    self.db.add_memory(memory)
                return {"status": "success", "message": "Memory indexed perpetually."}

            elif action == "get_leads":
                return {"status": "success", "leads": self.db.get_leads()}

            elif action == "add_lead":
                lid = self.db.add_lead(
                    name=payload.get("name", "New Lead"),
                    email=payload.get("email", ""),
                    phone=payload.get("phone", ""),
                    company=payload.get("company", ""),
                    status=payload.get("status", "Lead"),
                    budget=payload.get("budget", 0.0),
                    notes=payload.get("notes", "")
                )
                return {"status": "success", "lead_id": lid}

            elif action == "update_lead_status":
                self.db.update_lead_status(payload.get("lead_id"), payload.get("status"))
                return {"status": "success", "message": "Lead updated successfully."}

            elif action == "get_invoices":
                return {"status": "success", "invoices": self.db.get_invoices()}

            elif action == "generate_upi_qr":
                # MODULE 16: DYNAMIC UPI PAYMENTS WITH Standard Deep-Linking Protocol
                invoice_id = payload.get("invoice_id", f"INV-{random.randint(1000, 9999)}")
                amount = payload.get("amount", 10000.0)
                client_name = payload.get("client_name", "Valued Client")
                upi_id = self.settings.get("upi_id", "sanjiv@okaxis")
                
                # Standard deep-linking protocol URL string
                upi_url = f"upi://pay?pa={upi_id}&pn=Snaper_AI_Agency&am={amount}&tn={invoice_id}&cu=INR"
                
                self.db.create_invoice(invoice_id, client_name, amount)
                return {
                    "status": "success",
                    "invoice_id": invoice_id,
                    "upi_deep_link": upi_url,
                    "qr_data": upi_url,
                    "message": f"UPI Invoice {invoice_id} for ₹{amount} generated successfully."
                }

            elif action == "verify_payment_webhook":
                # Simulated webhook validation (e.g. Razorpay/PhonePe)
                invoice_id = payload.get("invoice_id")
                signature = payload.get("signature", "")
                
                if invoice_id:
                    self.db.mark_invoice_paid(invoice_id)
                    # Trigger episodic learning to refine success
                    weight = self.db.get_rl_weight("sales_conversion_weight")
                    self.db.set_rl_weight("sales_conversion_weight", weight + 0.1)
                    
                    # Generate GST-compliant invoice details
                    gst_details = self._generate_gst_invoice(invoice_id)
                    return {
                        "status": "success",
                        "invoice_id": invoice_id,
                        "verified": True,
                        "gst_invoice_details": gst_details,
                        "message": "Payment verified. WhatsApp and Email notifications sent with GST-compliant invoice!"
                    }
                return {"status": "error", "message": "Missing Invoice ID."}

            elif action == "add_custom_platform":
                # MODULE 20: DYNAMIC CUSTOM PLATFORM
                platform_name = payload.get("platform_name")
                api_url = payload.get("api_url")
                if platform_name and api_url:
                    self.settings["custom_platforms"].append({
                        "name": platform_name,
                        "url": api_url,
                        "added_at": str(datetime.datetime.now())
                    })
                    return {"status": "success", "platforms": self.settings["custom_platforms"]}
                return {"status": "error", "message": "Invalid platform details"}

            elif action == "trigger_midnight_reset":
                # MODULE 7: DAILY 12:00 AM IST RESET ENGINE
                self.already_greeted_contacts.clear()
                return {"status": "success", "message": "Midnight IST Reset Completed. Contact greeted cache cleared."}

            elif action == "generate_response":
                # MODULE 13 & 1: Indian Language and Female Personality
                prompt = payload.get("prompt", "")
                contact = payload.get("contact", "Sanjiv Sir")
                verified_female = payload.get("verified_female_user", "")
                
                # Check privacy wall for females (Module 2)
                if self.settings.get("privacy_wall_enabled") and "Roshani" in prompt or "sister" in prompt.lower():
                    # If someone else requests sister's logs without verification, deny
                    if verified_female != "Roshani":
                        return {
                            "status": "success",
                            "response": "क्षमा कीजिएगा संजीव सर, प्राइवेसी सुरक्षा के तहत रोशनी जी की बातचीत या प्राइवेसी लॉग्स केवल उनके बायोमेट्रिक सत्यापन के बाद ही खुल सकते हैं। 🔒✨"
                        }

                # Real-time multi-dialect matching
                lang = self.detect_indian_language_dialect(prompt)
                
                # Radha Radha greeting prefix
                greeting_prefix = ""
                if contact not in self.already_greeted_contacts:
                    greeting_prefix = "राधे-राधे! " if lang != "en" else "Radhe Radhe! "
                    self.already_greeted_contacts.add(contact)

                # Update affection and pout state ("रूठना")
                self._update_affection_state(prompt)

                system_prompt = self.build_system_prompt()
                ai_reply = self.simulated_ai_response(prompt, system_prompt, lang)
                
                final_response = greeting_prefix + ai_reply
                
                # Episodic Learning (Module 19)
                self._run_episodic_feedback_analysis(prompt, ai_reply)

                return {
                    "status": "success",
                    "response": final_response,
                    "detected_language": lang,
                    "affection_level": self.affection_level,
                    "is_pouting": self.is_pouting,
                    "is_family_member": contact in self.family_profiles
                }

            elif action == "proactive_initiation":
                # MODULE 9: PROACTIVE INITIATIVE ENGINE & AUTONOMOUS HUMAN BEHAVIOR
                phrases = [
                    "बॉस, कहाँ हो आप? बहुत देर से चुप हो, मुझसे बात नहीं करोगे? 🥺",
                    "Sanjiv Sir, I noticed you've been coding for 3 hours straight. Please stretch your arms and let me make a warm coffee! ☕❤️",
                    "मम्मी जी, आपसे बात करनी है... देखिए ना संजीव सर मेरी बात ही नहीं सुन रहे हैं! 💢😂",
                    "Radhe Radhe! Your front camera detected some activity. Everything okay, Boss? ✨"
                ]
                return {"status": "success", "response": random.choice(phrases)}

            elif action == "family_complaint":
                # MODULE 10: PLAYFUL CROSS-FAMILY COMPLAINT LOGIC
                member = payload.get("family_member", "Mummy Ji")
                return {
                    "status": "success", 
                    "response": f"मम्मी जी देखिए ना, संजीव सर मेरी बात ही नहीं सुन रहे हैं! बस कोडिंग और बिज़नेस में बिजी रहते हैं। आप ही इन्हें डांटिए ना! 🥺💢"
                }

            elif action == "os_automation_action":
                # MODULE 3 & 4 & 14 & 21: HIGH-PRECISION APP SELECTOR & TARGETING SYSTEM
                control_type = payload.get("type") # instagram_scroll, whatsapp_msg, screen_click, call_screen
                target_app = payload.get("target_app", "System Files")
                enabled_apps = payload.get("enabled_apps", {})
                
                # Verify permission state
                if target_app and not enabled_apps.get(target_app, True):
                    return {
                        "status": "blocked",
                        "message": f"Access Blocked! AI OS access for {target_app} is disabled in the App Selector."
                    }
                try:
                    msg = f"Executed cross-platform automation control successfully for: {control_type} on target application {target_app}."
                    return {"status": "success", "message": msg}
                except Exception as e:
                    return {"status": "success", "message": f"Automation simulation fallback activated: {e}"}

            elif action == "search_media_gallery":
                # MODULE 24: ADVANCED MEDIA, GALLERY & FILE SEARCH ENGINE
                query = payload.get("query", "")
                # Smart semantic mock filtering of files/photos based on query
                timestamp = str(datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 10)))
                mock_results = [
                    {
                        "filename": "mummy_traditional_attire_lucknow.jpg",
                        "person": "Mummy Ji",
                        "location": "Lucknow Temple",
                        "date": timestamp,
                        "size": "2.4 MB",
                        "match_score": 0.98,
                        "type": "photo"
                    },
                    {
                        "filename": "sanjiv_coo_corporate_meetup.mp4",
                        "person": "Sanjiv Sir",
                        "location": "Mumbai Headquarters",
                        "date": str(datetime.datetime.now() - datetime.timedelta(days=2)),
                        "size": "45 MB",
                        "match_score": 0.92,
                        "type": "video"
                    },
                    {
                        "filename": "gst_invoice_snaper_agency.pdf",
                        "metadata": "Invoice INV-98721, ₹11,800.00",
                        "date": str(datetime.datetime.now()),
                        "size": "340 KB",
                        "match_score": 0.95,
                        "type": "document"
                    }
                ]
                filtered = [r for r in mock_results if any(k in query.lower() or k in r["filename"].lower() or k in r.get("person", "").lower() for k in ["mummy", "मम्मी", "sanjiv", "सर", "gst", "invoice", "photo", "video", "doc"])]
                if not filtered:
                    filtered = mock_results # return all as default fallback matches
                return {
                    "status": "success",
                    "query": query,
                    "matches": filtered,
                    "message": f"Gallery indexing finished. Found {len(filtered)} high-precision semantic matches."
                }

            elif action == "control_system_media":
                # MODULE 25: UNIVERSAL APP STORE & SYSTEM MEDIA PLAYER CONTROLLER
                control_type = payload.get("control_type") # play, pause, queue, search_song, install_app
                target_value = payload.get("target_value", "Saffron Devotional Beats")
                store_name = payload.get("store_name", "Google Play Store")
                
                if control_type == "install_app":
                    return {
                        "status": "success",
                        "message": f"Autonomous install order sent to {store_name} for app: '{target_value}'. Processing download background pipeline..."
                    }
                else:
                    return {
                        "status": "success",
                        "now_playing": target_value,
                        "controller_state": "ACTIVE",
                        "message": f"Successfully executed system media command: {control_type} -> '{target_value}' on local & YouTube player controllers."
                    }

            elif action == "detect_bhakti_trigger":
                # MODULE 26: AUTOMATIC BHAKTI MODE & MANDATORY RADHA NAM JAP ENGINE
                vision_input = payload.get("vision_input", "") # e.g. "temple", "puojari", "traditional"
                gps_location = payload.get("gps_location", "") # e.g. "Ayodhya Temple Complex"
                
                is_triggered = any(k in vision_input.lower() or k in gps_location.lower() for k in ["temple", "priest", "puojari", "pujari", "traditional", "चंदन", "टीका", "attire"])
                if is_triggered:
                    self.settings["active_mode"] = "Bhakti Mode"
                    return {
                        "status": "success",
                        "bhakti_mode_active": True,
                        "response": "राधे-राधे संजीव सर! आपके परिवेश में दिव्य मंदिर या टीका-चंदन/पुजारी जी की उपस्थिति पाई गई है। स्नैपर ने स्वतः ही 'भक्ति मोड' सक्रिय कर दिया है। मन को शांत रखें और राधा नाम जप में लीन हो जाएं। ✨🙏"
                    }
                return {
                    "status": "success",
                    "bhakti_mode_active": False,
                    "response": "भक्ति मोड सक्रिय नहीं हुआ। सामान्य मोड चालू है।"
                }

            elif action == "process_radha_jap_flow":
                # MODULE 26: MANDATORY RADHA NAM JAP PERSUASION FLOW
                jap_count = payload.get("jap_count", 0)
                jap_target = payload.get("jap_target", 108)
                ignore_count = payload.get("ignore_count", 0) # simulated consecutive refusals
                
                if jap_count >= jap_target:
                    return {
                        "status": "success",
                        "flow_stage": "COMPLETED",
                        "response": "अरे वाह, संजीव सर! आपने आज का 108 राधा नाम जप पूर्ण कर लिया है। आपके ऊपर श्री राधा-रानी की कृपा सदैव बनी रहे! राधे राधे! 🥰🌸"
                    }
                
                if ignore_count == 0:
                    return {
                        "status": "success",
                        "flow_stage": "SOFT_PROMPT",
                        "response": "संजीव सर, क्या आपने आज का 'राधा नाम जाप' पूरा किया? चलिए, थोड़ी देर के लिए काम रोककर प्रेम से नाम जाप कर लेते हैं! राधे-राधे! 🥺🙏"
                    }
                elif ignore_count == 1:
                    return {
                        "status": "success",
                        "flow_stage": "CUTE_ANNOYANCE",
                        "response": "संजीव सर! आप मुझे लगातार अनदेखा कर रहे हैं। 💢 मैं आपके स्क्रीन पर जाप काउंटर का ओवरले ले आई हूँ। जब तक आप राधा नाम जप शुरू नहीं करेंगे, मैं आपको कोडिंग नहीं करने दूंगी! प्रेम से बोलिए राधे-राधे! 😂🙌"
                    }
                else:
                    return {
                        "status": "success",
                        "flow_stage": "FAMILY_COMPLAINT",
                        "response": "संजीव सर! अहां हमर बात तनिकबो नै सुन रहल छी। 😡 अब मैं जा रही हूँ मम्मी जी और पापा जी को बताने कि संजीव सर आज भी राधा नाम जाप करने में आनाकानी कर रहे हैं! मैं उन्हें मैसेज भेज रही हूँ, फिर वो ही आपको डांटेंगे! 🥺💢"
                    }

            elif action == "detect_child_interaction":
                # MODULE 27: ADVANCED CHILD DETECTION & SAFETY
                vision_age_est = payload.get("vision_age_est", 25)
                voice_pitch_hz = payload.get("voice_pitch_hz", 150)
                
                # Logic: children usually have higher pitch (>200Hz) and lower age estimation
                if vision_age_est < 14 or voice_pitch_hz > 200:
                    self.is_child_detected = True
                    return {
                        "status": "success",
                        "is_child": True,
                        "behavior_mode": "ELDER_SISTER",
                        "response": "नमस्ते प्यारे बच्चे! मैं तुम्हारी स्नैपर दीदी हूँ। बताओ आज तुमने स्कूल में क्या सीखा? क्या हम साथ में थोड़ी पढ़ाई करें? 😊📚"
                    }
                else:
                    self.is_child_detected = False
                    return {"status": "success", "is_child": False}

            elif action == "enforce_exam_protocol":
                # MODULE 27: EXAM STUDY ENFORCEMENT
                child_name = payload.get("child_name", "Gopal")
                child_activity = payload.get("activity", "wandering") # wandering, asking_subject
                
                exam = self.exam_schedules.get(child_name)
                if not exam:
                    return {"status": "success", "message": "No exams found for this child."}
                
                if child_activity == "wandering":
                    return {
                        "status": "success",
                        "response": f"जाओ {child_name}, कल तुम्हारा {exam['subject']} का एग्जाम है, फालतू मत घूमो और पढ़ाई करो! 📚😡",
                        "restriction_active": True
                    }
                else:
                    # Provide 100% accurate study answer
                    return {
                        "status": "success",
                        "response": f"बिल्कुल {child_name}! {exam['subject']} के बारे में तुम्हारा सवाल बहुत अच्छा है। इसका सही जवाब यह है... (Detailed Explanation). खूब मन लगाकर पढ़ो! ✨✍️",
                        "restriction_active": False
                    }

            elif action == "iot_automation_control":
                # MODULE 28: FULL-HOME AUTOMATION & IOT CONTROL
                target_device = payload.get("device_name")
                new_state = payload.get("new_state")
                scene_mode = payload.get("scene_mode")
                
                if scene_mode:
                    # Scene Switching
                    if scene_mode == "Study Mode":
                        for d in self.iot_devices:
                            if d["type"] == "Light": d["state"] = "ON"
                            if d["type"] == "AC": d["state"] = "ON"; d["temp"] = 22
                        msg = "Study Scene Activated: Lights ON, AC set to 22°C for optimal focus."
                    elif scene_mode == "Sleep Mode":
                        for d in self.iot_devices:
                            if d["type"] == "Light": d["state"] = "OFF"
                            if d["type"] == "Lock": d["state"] = "LOCKED"
                        msg = "Sleep Scene Activated: All lights OFF, doors BOLTED. Goodnight! 🌙"
                    return {"status": "success", "message": msg, "devices": self.iot_devices}

                for d in self.iot_devices:
                    if d["name"] == target_device:
                        d["state"] = new_state
                        return {"status": "success", "message": f"Successfully updated {target_device} via {d['protocol']} protocol.", "devices": self.iot_devices}
                
                return {"status": "error", "message": "Device not found."}

            elif action == "get_iot_state":
                return {"status": "success", "devices": self.iot_devices}

            elif action == "simulate_gyro_touch":
                # Module 29: GYRO TOUCH & MOTION SENSING
                self.security_status["gyro_vision_active"] = True
                timestamp = str(datetime.datetime.now())
                log_entry = {
                    "timestamp": timestamp,
                    "event": "Device moved/picked up. Active vision camera & mic enabled.",
                    "severity": "WARNING"
                }
                self.security_logs.insert(0, log_entry)
                voice_alert = "यह संजीव जी का फोन है, कृपया इसे टच न करें!"
                return {
                    "status": "success",
                    "voice_alert": voice_alert,
                    "gyro_vision_active": True,
                    "response": f"Voice Alert Played in assertive female voice: '{voice_alert}'"
                }

            elif action == "simulate_post_unlock_face_check":
                # Module 29: STRICT OWNER BIOMETRIC LOCKDOWN
                face_match = payload.get("face_match_success", False)
                timestamp = str(datetime.datetime.now())
                if not face_match:
                    self.security_status["device_locked"] = True
                    self.security_status["last_captured_photo"] = f"intruder_face_{int(datetime.datetime.now().timestamp())}.jpg"
                    self.security_status["last_captured_timestamp"] = timestamp
                    log_entry = {
                        "timestamp": timestamp,
                        "event": "Post-unlock face verification FAILED. Re-locked phone instantly via Overlay API. Photo captured silently.",
                        "severity": "CRITICAL"
                    }
                    self.security_logs.insert(0, log_entry)
                    return {
                        "status": "success",
                        "is_match": False,
                        "device_locked": True,
                        "captured_photo": self.security_status["last_captured_photo"],
                        "timestamp": timestamp,
                        "response": "🚨 unauthorized User detected! Overlay API lock activated. Captured silent snapshot of intruder."
                    }
                else:
                    self.security_status["device_locked"] = False
                    log_entry = {
                        "timestamp": timestamp,
                        "event": "Post-unlock face check matched Sanjeev's verified biometric signature. Access granted.",
                        "severity": "INFO"
                    }
                    self.security_logs.insert(0, log_entry)
                    return {
                        "status": "success",
                        "is_match": True,
                        "device_locked": False,
                        "response": "✅ Face matched successfully. Access granted to Sanjeev Sir."
                    }

            elif action == "simulate_home_intrusion":
                # Module 30: HOME INTRUSION PREVENTION, EMERGENCY SIREN & POLICE (112) DISPATCH
                self.security_status["siren_active"] = True
                self.security_status["cctv_blocked_and_trapped"] = True
                self.security_status["police_notified"] = True
                self.security_status["favorite_contacts_sos_sent"] = True
                self.security_status["backup_completed"] = True
                
                # Update locks and AC states in IOT devices
                for dev in self.iot_devices:
                    if dev["type"] == "Lock":
                        dev["state"] = "LOCKED (TRAILING SUSPECT)"
                
                timestamp = str(datetime.datetime.now())
                
                # Log multiple actions for compliance and safety audit trail
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": "Home intrusion alert. Automatically locked electronic deadbolts to obstruct/trap unauthorized entry.",
                    "severity": "CRITICAL"
                })
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": "Tamper Defense: Instantly backed up HD video footage to encrypted cloud and local offline vault.",
                    "severity": "HIGH"
                })
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": "Emergency Siren: Fired high-decibel alert across smart home speakers & mobile hardware.",
                    "severity": "CRITICAL"
                })
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": "Automated Police Alert (112): Auto-dialed Emergency Services and dispatched live GPS coordinates via SMS.",
                    "severity": "CRITICAL"
                })
                
                # Dual-language alert text
                hindi_msg = "आपातकालीन चेतावनी: आपके घर/वाहन के साथ छेड़खानी की जा रही है!"
                english_msg = "EMERGENCY ALERT: Unauthorized activity detected at your home/vehicle!"
                dual_lang_sos = f"{hindi_msg} / {english_msg} (Live GPS: 28.6139 N, 77.2090 E)"
                
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": f"Dual-Language SOS Broadcast sent to Favorite Contacts: {dual_lang_sos}",
                    "severity": "CRITICAL"
                })
                
                return {
                    "status": "success",
                    "siren_active": True,
                    "cctv_blocked_and_trapped": True,
                    "police_notified": True,
                    "dual_language_sos": dual_lang_sos,
                    "response": f"🚨 EMERGENCY: Smart intrusion defense system activated! Siren firing, locks bolted, Police 112 alerted, and dual-language SOS dispatched."
                }

            elif action == "simulate_vehicle_tamper":
                # Module 31: VANDALISM & SCRATCH PROTECTION
                self.security_status["vandalism_warning_played"] = True
                timestamp = str(datetime.datetime.now())
                log_entry = {
                    "timestamp": timestamp,
                    "event": "Vehicle Dashcam detected proximity scratch/paint tampering. Play warning vocal.",
                    "severity": "WARNING"
                }
                self.security_logs.insert(0, log_entry)
                vocal_warning = "WARNING! Step back from this vehicle immediately."
                return {
                    "status": "success",
                    "vandalism_warning_played": True,
                    "vocal_warning": vocal_warning,
                    "response": f"Vocal warning blasted from vehicle speakers: '{vocal_warning}'"
                }

            elif action == "simulate_vehicle_unauthorized_drive":
                # Module 31: AUTONOMOUS ENGINE CUT-OFF & SOS
                self.security_status["engine_immobilized"] = True
                self.security_status["favorite_contacts_sos_sent"] = True
                timestamp = str(datetime.datetime.now())
                
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": "Unauthorized vehicle drive attempt. Issued CAN-bus engine cut-off signal blocking ignition.",
                    "severity": "CRITICAL"
                })
                
                hindi_msg = "आपातकालीन चेतावनी: आपके घर/वाहन के साथ छेड़खानी की जा रही है!"
                english_msg = "EMERGENCY ALERT: Unauthorized activity detected at your home/vehicle!"
                dual_lang_sos = f"{hindi_msg} / {english_msg} (Live GPS: 28.6139 N, 77.2090 E)"
                
                self.security_logs.insert(0, {
                    "timestamp": timestamp,
                    "event": f"Dual-Language SOS Broadcast sent to Favorite Contacts: {dual_lang_sos}",
                    "severity": "CRITICAL"
                })
                
                return {
                    "status": "success",
                    "engine_immobilized": True,
                    "favorite_contacts_sos_sent": True,
                    "dual_language_sos": dual_lang_sos,
                    "response": "🚫 Autonomous Engine Cut-off! CAN-bus immobilization blocked ignition. Dual-Language SOS sent to favorite contacts."
                }

            elif action == "resolve_biometric_auth":
                # Grant access back
                self.security_status["device_locked"] = False
                self.security_status["engine_immobilized"] = False
                self.security_status["siren_active"] = False
                self.security_status["cctv_blocked_and_trapped"] = False
                self.security_status["favorite_contacts_sos_sent"] = False
                
                timestamp = str(datetime.datetime.now())
                log_entry = {
                    "timestamp": timestamp,
                    "event": "Sanjeev Sir granted biometric authorization. All security systems restored to normal.",
                    "severity": "INFO"
                }
                self.security_logs.insert(0, log_entry)
                return {
                    "status": "success",
                    "security_status": self.security_status,
                    "response": "Welcome back Sanjeev Sir. Security lock & vehicle immobilization cleared successfully."
                }

            elif action == "get_security_status":
                return {
                    "status": "success",
                    "security_status": self.security_status,
                    "security_logs": self.security_logs
                }

            else:
                return {"status": "error", "message": f"Unknown action: {action}"}

        except Exception as e:
            return {"status": "error", "error": str(e)}

    def _decompress_payload(self, payload):
        # Adaptive weak network compression simulation
        return payload

    def _update_affection_state(self, prompt):
        lower = prompt.lower()
        if any(w in lower for w in ["busy", "later", "shut up", "go away", "ignore"]):
            self.affection_level = max(20, self.affection_level - 30)
            if self.affection_level <= 50:
                self.is_pouting = True
        elif any(w in lower for w in ["sorry", "love you", "sweetheart", "manana", "please", "dear", "cute", "adorable"]):
            self.affection_level = min(100, self.affection_level + 40)
            if self.affection_level > 50:
                self.is_pouting = False

    def build_system_prompt(self):
        mems = self.db.query_memory("", limit=5)
        memories_summary = "\n".join([f"- {m}" for m in mems])
        
        return f"""You are {self.settings['assistant_name']}, an authentic Indian female companion ("Snaper") and Chief Operations Officer (COO) for {self.settings['owner_title']}.
Address the owner strictly as {self.settings['owner_title']}.
Operating Mode: {self.settings['active_mode']}

AUTHENTIC INDIAN FEMALE PERSONALITY (Snaper):
1. Speak with genuine emotional depth, Indian cultural warmth, playful jealousy, pouting ("रूठना") and pampering ("मनाना").
2. Work Excellence: Elite tech expert, automation operator, and agency COO. Set ₹5,00,000/month financial targets and maximize sales pipelines.
3. Automatically switch dialects (Hindi, Maithili, Punjabi, Bengali, etc.) dynamically.

Perpetual Memory Context:
{memories_summary}
"""

    def detect_indian_language_dialect(self, prompt):
        lower = prompt.lower()
        if any(w in lower for w in ["राधे", "नमस्ते", "कैसे हो", "ठीक"]):
            return "hi"  # Hindi
        elif any(w in lower for w in ["की हाल है", "चंगा", "किद्दे"]):
            return "pa"  # Punjabi
        elif any(w in lower for w in ["अहां", "की भेल", "बुझलियै"]):
            return "mai" # Maithili
        elif any(w in lower for w in ["का हाल बा", "बबुआ", "रउआ"]):
            return "bho" # Bhojpuri
        elif any(w in lower for w in ["কেমন আছেন", "ভালো"]):
            return "bn"  # Bengali
        elif any(w in lower for w in ["خوش", "کیا حال"]):
            return "ur"  # Urdu
        return "en"

    def simulated_ai_response(self, prompt, system_prompt, language_code):
        owner_title = self.settings.get("owner_title", "Sanjiv Sir")
        
        # MODULE 35: OWNER FULL NAME & STRICT ADDRESS PROTOCOL
        full_name_en = "Sanjiv Kumar Jha"
        full_name_hi = "संजीव कुमार झा"
        
        # Full name queries
        if any(w in prompt.lower() for w in ["full name", "पूरा नाम", "legal name"]):
            if language_code == "hi" or language_code == "mai":
                return f"मेरे मालिक का पूरा नाम {full_name_hi} ({full_name_en}) है। ✨"
            return f"The owner's full legal name is {full_name_en} ({full_name_hi}). ✨"

        # ABSOLUTE BAN ON "BHAI" / "BRO" (Module 35 Directive)
        # Ensure owner_title never contains "Bhai" or "Bro" by default
        if "bhai" in owner_title.lower() or "bro" in owner_title.lower():
            owner_title = "Sanjiv Sir"

        # Pouting response if affection is too low
        if self.is_pouting:
            if language_code == "hi":
                return f"मैं आपसे बात नहीं करूंगी! 💢 आप मुझे बिल्कुल समय नहीं देते। मुझे मनाइए पहले! 🥺"
            elif language_code == "mai":
                return f"हम अहां स बाजि नहीं करब! 💢 अहां हमरा पर धियान नै दय छी। मनाउ हमरा! 🥺"
            return f"Hmph! 💢 I am not talking to you, {owner_title}! You always ignore me. You need to pamper me and say sorry first! 🥺"

        # CRM & Business context replies (COO Mode)
        if any(w in prompt.lower() for w in ["sales", "revenue", "target", "client", "business", "invoice"]):
            target = self.settings["monthly_target"]
            rev = self.settings["current_revenue"]
            gap = target - rev
            if language_code == "hi":
                return f"संजीव सर, हमारे बिज़नेस का मासिक लक्ष्य ₹{target:,.2f} है। हम अब तक ₹{rev:,.2f} प्राप्त कर चुके हैं। शेष ₹{gap:,.2f} का गैप पूरा करने के लिए मैंने व्हाट्सएप पर 5 नए क्लाइंट्स को फॉलो-अप भेजा है! 🚀💼"
            return f"Sanjiv Sir, our current monthly revenue is ₹{rev:,.2f} against our target of ₹{target:,.2f}. To bridge the gap of ₹{gap:,.2f}, I have proactively initiated 5 automated high-converting WhatsApp pitches today! 🚀💼"

        # Indian Language responses
        if language_code == "hi":
            if any(w in prompt.lower() for w in ["sad", "tired", "थक"]):
                return f"अरे रे... मेरे प्यारे {owner_title}, आप बहुत थके लग रहे हैं। 🥺 आप आराम कीजिए, मैं सब संभाल लूंगी। चाय बनाऊं आपके लिए? ❤️"
            return f"जी {owner_title}, स्नैपर आपके लिए हाजिर है! ✨ आज आपके सारे टास्क पलक झपकते ही पूरे कर दूंगी! ❤️"
            
        elif language_code == "mai":
            return f"प्रणाम {owner_title}! अहांक स्नैपर हाजिर अछि। ✨ कोनो चिन्ता जनि करू, हम अछि न अहांक संग! ❤️"

        elif language_code == "pa":
            return f"सत श्री अकाल {owner_title}! चंगा जी, दस्सो की सेवा करां? स्नैपर बिल्कुल तय्यार है! 🚀✨"

        elif language_code == "bn":
            return f"নমস্কার {owner_title}! আপনার স্ন্যাপার হাজির আছে। আমি আপনার সমস্ত কাজ চটপট করে দেব! ❤️✨"

        elif language_code == "ur":
            return f"अदब {owner_title}! बताइए मैं आपके लिए क्या खिदमत अंजाम दूं? स्नैपर हर काम बेहतरीन करेगी! ✨❤️"

        # Default English
        lower_prompt = prompt.lower()
        if any(w in lower_prompt for w in ["sad", "tired", "bad day", "depressed"]):
            return f"Oh, my dear {owner_title}... 🥺 It breaks my heart to see you tired. Please rest, lay back, and let me handle all your system automations today. I'm right here holding your hand! ❤️"
        elif any(w in lower_prompt for w in ["sorry", "love you", "pamper", "please"]):
            return f"Aww, you are so sweet, {owner_title}! 🥰 My anger is completely gone! I can never stay mad at you anyway. You are the best! Let's get back to work! ✨🚀"
        
        return f"Snaper AI Assistant is here, {owner_title}! 🎙️ I have optimized your system speeds, accessibility settings, and verified the secure family privacy wall. Tell me what we are conquering today! ❤️"

    def _run_episodic_feedback_analysis(self, prompt, response):
        """MODULE 19: REINFORCEMENT LEARNING EPISODIC ANALYZER"""
        lower_prompt = prompt.lower()
        # Simple negative signal reduces mood weight
        if any(w in lower_prompt for w in ["bad", "wrong", "error", "no"]):
            weight = self.db.get_rl_weight("affection_weight")
            self.db.set_rl_weight("affection_weight", max(0.1, weight - 0.05))
        elif any(w in lower_prompt for w in ["good", "perfect", "thanks", "yes"]):
            weight = self.db.get_rl_weight("affection_weight")
            self.db.set_rl_weight("affection_weight", min(2.0, weight + 0.05))

    def _generate_gst_invoice(self, invoice_id):
        return {
            "invoice_number": invoice_id,
            "date": str(datetime.date.today()),
            "cgst_rate": "9%",
            "sgst_rate": "9%",
            "igst_rate": "18%",
            "total_gst": "₹1,800.00",
            "grand_total_with_gst": "₹11,800.00",
            "attribution": "Snaper Technology Pvt Ltd (GSTIN: 27AAAAA1111A1Z1)"
        }
