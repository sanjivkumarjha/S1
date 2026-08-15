import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Secure on-device credential store for the Snaper AI Assistant Flutter app.
///
/// Uses `flutter_secure_storage` (Android Keystore / iOS Keychain) so API keys
/// are NEVER persisted in plain SharedPreferences or app code. Keys are
/// only written/read at runtime and never ship inside the binary.
class SecureCredentialsService {
  static const _storage = FlutterSecureStorage(
    aOptions: AndroidOptions(encryptedSharedPreferences: true),
  );

  static const _universalApiKey = 'user_api_key';
  static const _geminiApiKey = 'gemini_api_key';
  static const _openaiApiKey = 'openai_api_key';
  static const _claudeApiKey = 'claude_api_key';
  static const _grokApiKey = 'grok_api_key';
  static const _nvidiaApiKey = 'nvidia_api_key';
  static const _openRouterApiKey = 'openrouter_api_key';
  static const _customBaseUrl = 'custom_base_url';

  // Universal / Primary Key
  static Future<void> saveUniversalApiKey(String key) async {
    if (key.isEmpty) {
      await _storage.delete(key: _universalApiKey);
    } else {
      await _storage.write(key: _universalApiKey, value: key);
    }
  }

  static Future<String?> getUniversalApiKey() async =>
      await _storage.read(key: _universalApiKey);

  // Per-Provider Keys
  static Future<void> saveGeminiApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _geminiApiKey) : await _storage.write(key: _geminiApiKey, value: key);
  static Future<String?> getGeminiApiKey() async => await _storage.read(key: _geminiApiKey);

  static Future<void> saveOpenAiApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _openaiApiKey) : await _storage.write(key: _openaiApiKey, value: key);
  static Future<String?> getOpenAiApiKey() async => await _storage.read(key: _openaiApiKey);

  static Future<void> saveClaudeApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _claudeApiKey) : await _storage.write(key: _claudeApiKey, value: key);
  static Future<String?> getClaudeApiKey() async => await _storage.read(key: _claudeApiKey);

  static Future<void> saveGrokApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _grokApiKey) : await _storage.write(key: _grokApiKey, value: key);
  static Future<String?> getGrokApiKey() async => await _storage.read(key: _grokApiKey);

  static Future<void> saveNvidiaApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _nvidiaApiKey) : await _storage.write(key: _nvidiaApiKey, value: key);
  static Future<String?> getNvidiaApiKey() async => await _storage.read(key: _nvidiaApiKey);

  static Future<void> saveOpenRouterApiKey(String key) async =>
      key.isEmpty ? await _storage.delete(key: _openRouterApiKey) : await _storage.write(key: _openRouterApiKey, value: key);
  static Future<String?> getOpenRouterApiKey() async => await _storage.read(key: _openRouterApiKey);

  static Future<void> saveCustomBaseUrl(String url) async =>
      url.isEmpty ? await _storage.delete(key: _customBaseUrl) : await _storage.write(key: _customBaseUrl, value: url);
  static Future<String?> getCustomBaseUrl() async => await _storage.read(key: _customBaseUrl);

  static Future<void> clearAll() async {
    await _storage.deleteAll();
  }
}