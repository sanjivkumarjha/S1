import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';
import 'package:snaper_ai_assistant/services/secure_credentials_service.dart';

class AiModelSettingsScreen extends StatefulWidget {
  @override
  _AiModelSettingsScreenState createState() => _AiModelSettingsScreenState();
}

class _AiModelSettingsScreenState extends State<AiModelSettingsScreen> {
  final TextEditingController _geminiKeyController = TextEditingController();
  final TextEditingController _openaiKeyController = TextEditingController();
  final TextEditingController _anthropicKeyController = TextEditingController();
  final TextEditingController _grokKeyController = TextEditingController();
  final TextEditingController _nvidiaKeyController = TextEditingController();
  final TextEditingController _openRouterKeyController = TextEditingController();
  final TextEditingController _customBaseUrlController = TextEditingController();
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadKeys();
  }

  Future<void> _loadKeys() async {
    final geminiKey = await SecureCredentialsService.getGeminiApiKey();
    final openaiKey = await SecureCredentialsService.getOpenAiApiKey();
    final claudeKey = await SecureCredentialsService.getClaudeApiKey();
    final grokKey = await SecureCredentialsService.getGrokApiKey();
    final nvidiaKey = await SecureCredentialsService.getNvidiaApiKey();
    final openRouterKey = await SecureCredentialsService.getOpenRouterApiKey();
    final customUrl = await SecureCredentialsService.getCustomBaseUrl();

    setState(() {
      _geminiKeyController.text = geminiKey ?? '';
      _openaiKeyController.text = openaiKey ?? '';
      _anthropicKeyController.text = claudeKey ?? '';
      _grokKeyController.text = grokKey ?? '';
      _nvidiaKeyController.text = nvidiaKey ?? '';
      _openRouterKeyController.text = openRouterKey ?? '';
      _customBaseUrlController.text = customUrl ?? '';
      _isLoading = false;
    });
  }

  @override
  void dispose() {
    _geminiKeyController.dispose();
    _openaiKeyController.dispose();
    _anthropicKeyController.dispose();
    _grokKeyController.dispose();
    _nvidiaKeyController.dispose();
    _openRouterKeyController.dispose();
    _customBaseUrlController.dispose();
    super.dispose();
  }

  Future<void> _saveSettings() async {
    // Save each key to encrypted local storage — NEVER in plain SharedPreferences.
    await SecureCredentialsService.saveGeminiApiKey(_geminiKeyController.text);
    await SecureCredentialsService.saveOpenAiApiKey(_openaiKeyController.text);
    await SecureCredentialsService.saveClaudeApiKey(_anthropicKeyController.text);
    await SecureCredentialsService.saveGrokApiKey(_grokKeyController.text);
    await SecureCredentialsService.saveNvidiaApiKey(_nvidiaKeyController.text);
    await SecureCredentialsService.saveOpenRouterApiKey(_openRouterKeyController.text);
    await SecureCredentialsService.saveCustomBaseUrl(_customBaseUrlController.text);

    // Update in-memory settings for other components to use
    final settings = Provider.of<UserSettings>(context, listen: false);
    final provider = _detectProvider();
    if (provider != null) {
      settings.setApiKey(provider, _geminiKeyController.text.isNotEmpty
          ? _geminiKeyController.text
          : _openaiKeyController.text.isNotEmpty
              ? _openaiKeyController.text
              : _anthropicKeyController.text.isNotEmpty
                  ? _anthropicKeyController.text
                  : _grokKeyController.text.isNotEmpty
                      ? _grokKeyController.text
                      : _openRouterKeyController.text);
    }
    settings.notifyListeners();

    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text("AI & API Settings Saved Securely (encrypted storage)!")),
      );
    }
  }

  String? _detectProvider() {
    if (_geminiKeyController.text.startsWith('AIzaSy')) return 'Google Gemini';
    if (_openaiKeyController.text.startsWith('sk-proj-')) return 'OpenAI';
    if (_anthropicKeyController.text.startsWith('sk-ant-')) return 'Anthropic';
    if (_grokKeyController.text.startsWith('xai-')) return 'xAI Grok';
    if (_nvidiaKeyController.text.isNotEmpty) return 'Nvidia';
    if (_openRouterKeyController.text.isNotEmpty) return 'OpenRouter';
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text("API & AI Model Settings", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: SingleChildScrollView(
        padding: EdgeInsets.all(16.0),
        child: Column(
          children: [
            GlassCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeader("Google Gemini API"),
                  _buildTextField("Gemini API Key", _geminiKeyController, isPassword: true),
                  SizedBox(height: 20),
                  
                  _buildHeader("OpenAI / ChatGPT"),
                  _buildTextField("OpenAI API Key", _openaiKeyController, isPassword: true),
                  SizedBox(height: 20),

                  _buildHeader("Anthropic Claude"),
                  _buildTextField("Claude API Key", _anthropicKeyController, isPassword: true),
                  SizedBox(height: 20),

                  _buildHeader("xAI Grok"),
                  _buildTextField("Grok API Key", _grokKeyController, isPassword: true),
                  SizedBox(height: 20),

                  _buildHeader("Nvidia NIM"),
                  _buildTextField("Nvidia API Key", _nvidiaKeyController, isPassword: true),
                  SizedBox(height: 20),

                  _buildHeader("OpenRouter"),
                  _buildTextField("OpenRouter API Key", _openRouterKeyController, isPassword: true),
                  SizedBox(height: 20),

                  _buildHeader("Custom LLM Endpoint"),
                  _buildTextField("Base URL (e.g. http://local:11434/v1)", _customBaseUrlController),
                  SizedBox(height: 30),

                  Center(
                    child: ElevatedButton(
                      onPressed: _saveSettings,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blueAccent,
                        padding: EdgeInsets.symmetric(horizontal: 40, vertical: 15),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: Text("Save Credentials", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                    ),
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Text(title, style: TextStyle(color: Colors.blueAccent, fontWeight: FontWeight.bold, fontSize: 14)),
    );
  }

  Widget _buildTextField(String label, TextEditingController controller, {bool isPassword = false}) {
    return TextField(
      controller: controller,
      obscureText: isPassword,
      style: TextStyle(color: Colors.white),
      decoration: InputDecoration(
        labelText: label,
        labelStyle: TextStyle(color: Colors.white38),
        enabledBorder: UnderlineInputBorder(borderSide: BorderSide(color: Colors.white12)),
        focusedBorder: UnderlineInputBorder(borderSide: BorderSide(color: Colors.blueAccent)),
      ),
    );
  }
}
