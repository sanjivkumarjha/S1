import json

class AssistantEngine:
    def __init__(self):
        # Local-first user settings and memory state mock/load
        self.settings = {
            "assistant_name": "Snaper",
            "owner_name": "Sanjiv Sir",
            "owner_title": "Sanjiv Sir",
            "active_mode": "All-Rounder",
            "language_code": "en"
        }
        self.memories = [
            "Owner prefers to be addressed as 'Sanjiv Sir'.",
            "Favorite greeting is 'Radhe Radhe'."
        ]

    def dispatch(self, request):
        action = request.get("action")
        payload = request.get("payload", {})

        if action == "ping":
            return {"status": "success", "response": "pong"}

        elif action == "get_settings":
            return {"status": "success", "settings": self.settings}

        elif action == "update_settings":
            self.settings.update(payload)
            return {"status": "success", "settings": self.settings}

        elif action == "get_memories":
            return {"status": "success", "memories": self.memories}

        elif action == "add_memory":
            memory = payload.get("memory")
            if memory and memory not in self.memories:
                self.memories.append(memory)
            return {"status": "success", "memories": self.memories}

        elif action == "generate_response":
            prompt = payload.get("prompt", "")
            system_prompt = self.build_system_prompt()
            
            # Simple simulation/stub logic for processing
            response_text = self.simulated_ai_response(prompt, system_prompt)
            return {
                "status": "success",
                "response": response_text,
                "model": "gemini-2.5-pro-exp-08",
                "provider": "Google Gemini"
            }

        else:
            return {"status": "error", "message": f"Unknown action: {action}"}

    def build_system_prompt(self):
        assistant_name = self.settings.get("assistant_name", "Snaper")
        owner_title = self.settings.get("owner_title", "Sanjiv Sir")
        active_mode = self.settings.get("active_mode", "All-Rounder")
        
        memories_summary = "\n".join([f"- {m}" for m in self.memories])
        
        base = f"""You are {assistant_name}, an affectionate, caring, emotionally intelligent digital companion for {owner_title}.
Address the owner as {owner_title}.
Operating Mode: {active_mode}

Memories:
{memories_summary}
"""
        return base

    def simulated_ai_response(self, prompt, system_prompt):
        # AI Provider processing fallback mock
        lower_prompt = prompt.lower()
        owner_title = self.settings.get("owner_title", "Sanjiv Sir")
        if "hello" in lower_prompt or "hi" in lower_prompt:
            return f"Radhe Radhe, {owner_title}! How can I help you today? I am your Snaper AI Assistant, built by Snaper Technology Pvt Ltd."
        elif "mode" in lower_prompt:
            return f"We are currently running in {self.settings.get('active_mode')} mode. You can toggle modes in settings anytime."
        else:
            return f"I hear you, {owner_title}. As your Snaper AI Assistant, I'm analyzing '{prompt}' to provide you the best support!"
