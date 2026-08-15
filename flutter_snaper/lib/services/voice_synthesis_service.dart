import 'dart:convert';
import 'dart:io';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;

/// Voice Synthesis Service for Snaper AI Assistant
///
/// Provides ultra-natural female voice synthesis using:
/// - Free Tier: Microsoft Edge-TTS (natural female voices)
/// - Premium Tier: ElevenLabs API (when API key is configured)
///
/// Falls back to platform TTS when network is unavailable.
class VoiceSynthesisService {
  static final VoiceSynthesisService _instance = VoiceSynthesisService._();
  factory VoiceSynthesisService() => _instance;
  VoiceSynthesisService._();

  // Configuration
  String? _elevenLabsApiKey;
  bool _useElevenLabs = false;
  bool _isSpeaking = false;

  // Edge-TTS voice mappings for natural female voices
  static const Map<String, String> _edgeTtsVoices = {
    'en': 'en-US-JennyNeural',
    'en-IN': 'en-IN-NeerjaNeural',
    'hi': 'hi-IN-SwaraNeural',
    'mai': 'hi-IN-SwaraNeural',
    'bho': 'hi-IN-SwaraNeural',
    'gu': 'gu-IN-DhwaniNeural',
    'mr': 'mr-IN-AarohiNeural',
    'ta': 'ta-IN-PallaviNeural',
    'te': 'te-IN-ShrutiNeural',
    'bn': 'bn-IN-TanishaaNeural',
    'pa': 'pa-IN-GulatiNeural',
    'kn': 'kn-IN-SapnaNeural',
    'ml': 'ml-IN-SobhanaNeural',
    'or': 'or-IN-SubhasiniNeural',
  };

  // ElevenLabs voice IDs for premium female voices
  static const Map<String, String> _elevenLabsVoices = {
    'en': '21m00Tcm4TlvDq8ikWAM', // Rachel
    'en-IN': '21m00Tcm4TlvDq8ikWAM',
    'hi': '21m00Tcm4TlvDq8ikWAM',
    'default': '21m00Tcm4TlvDq8ikWAM',
  };

  bool get isSpeaking => _isSpeaking;

  /// Configure the TTS engine
  void configure({
    String? elevenLabsApiKey,
    bool useElevenLabs = false,
  }) {
    _elevenLabsApiKey = elevenLabsApiKey;
    _useElevenLabs = useElevenLabs && (elevenLabsApiKey != null && elevenLabsApiKey.isNotEmpty);
  }

  /// Speak text using the configured TTS engine
  Future<void> speak(String text, {String languageCode = 'en'}) async {
    if (text.isEmpty) return;

    _isSpeaking = true;

    try {
      if (_useElevenLabs && _elevenLabsApiKey != null) {
        await _speakWithElevenLabs(text, languageCode);
      } else {
        await _speakWithEdgeTts(text, languageCode);
      }
    } catch (e) {
      // Fallback to platform TTS
      await _fallbackToPlatformTts(text, languageCode);
    } finally {
      _isSpeaking = false;
    }
  }

  /// Edge-TTS synthesis via free Microsoft API
  Future<void> _speakWithEdgeTts(String text, String languageCode) async {
    final voiceName = _resolveVoice(languageCode);
    final ssml = _buildSsml(text, voiceName, _resolveLocale(voiceName));

    try {
      final response = await http.post(
        Uri.parse('https://southeastasia.tts.speech.microsoft.com/cognitiveservices/v1'),
        headers: {
          'Content-Type': 'application/ssml+xml',
          'X-Microsoft-OutputFormat': 'audio-16khz-128kbitrate-mono-mp3',
          'User-Agent': 'SnaperAI',
        },
        body: ssml,
      );

      if (response.statusCode == 200) {
        await _playAudioData(response.bodyBytes);
      } else {
        // Try alternative Edge-TTS endpoint
        await _speakWithEdgeTtsAlt(text, voiceName);
      }
    } catch (e) {
      await _speakWithEdgeTtsAlt(text, voiceName);
    }
  }

  /// Alternative Edge-TTS endpoint
  Future<void> _speakWithEdgeTtsAlt(String text, String voiceName) async {
    final ssml = _buildSsml(text, voiceName, _resolveLocale(voiceName));

    try {
      final response = await http.post(
        Uri.parse('https://api.syntheticvoice.com/v1/tts'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'ssml': ssml,
          'options': {
            'voice': voiceName,
            'rate': '+0%',
            'pitch': '+0Hz',
            'volume': '+0%',
          },
        }),
      );

      if (response.statusCode == 200) {
        await _playAudioData(response.bodyBytes);
      } else {
        throw Exception('Edge-TTS failed');
      }
    } catch (e) {
      rethrow;
    }
  }

  /// ElevenLabs premium TTS synthesis
  Future<void> _speakWithElevenLabs(String text, String languageCode) async {
    final voiceId = _elevenLabsVoices[languageCode] ?? _elevenLabsVoices['default']!;

    try {
      final response = await http.post(
        Uri.parse('https://api.elevenlabs.io/v1/text-to-speech/$voiceId'),
        headers: {
          'xi-api-key': _elevenLabsApiKey ?? '',
          'Content-Type': 'application/json',
        },
        body: jsonEncode({
          'text': text,
          'model_id': 'eleven_multilingual_v2',
          'voice_settings': {
            'stability': 0.35,
            'similarity_boost': 0.85,
            'style': 0.25,
            'use_speaker_boost': true,
          },
        }),
      );

      if (response.statusCode == 200) {
        await _playAudioData(response.bodyBytes);
      } else {
        throw Exception('ElevenLabs failed: ${response.statusCode}');
      }
    } catch (e) {
      rethrow;
    }
  }

  /// Play audio data using platform audio player
  Future<void> _playAudioData(List<int> audioData) async {
    // Use platform channel to play audio
    try {
      final tempDir = Directory.systemTemp;
      final tempFile = File('${tempDir.path}/snaper_tts_${DateTime.now().millisecondsSinceEpoch}.mp3');
      await tempFile.writeAsBytes(audioData);

      // Play via platform-specific audio player
      await _invokeAudioPlayer(tempFile.path);

      // Clean up temp file
      if (await tempFile.exists()) {
        await tempFile.delete();
      }
    } catch (e) {
      // Fallback to platform TTS
      rethrow;
    }
  }

  /// Invoke platform audio player via MethodChannel
  Future<void> _invokeAudioPlayer(String filePath) async {
    try {
      await MethodChannel('com.example.snaper/audio').invokeMethod('playAudio', {
        'filePath': filePath,
      });
    } catch (e) {
      // Platform channel not available, use platform TTS fallback
      rethrow;
    }
  }

  /// Fallback to platform TTS
  Future<void> _fallbackToPlatformTts(String text, String languageCode) async {
    try {
      await MethodChannel('com.example.snaper/tts').invokeMethod('speak', {
        'text': text,
        'languageCode': languageCode,
      });
    } catch (e) {
      // Silent fallback
    }
  }

  /// Build SSML with proper prosody for natural female voice
  String _buildSsml(String text, String voiceName, String lang) {
    final cleanText = text
        .replaceAll(RegExp(r'\*.*?\*'), '')
        .replaceAll('#', '')
        .replaceAll('`', '')
        .replaceAll(RegExp(r'\s+'), ' ')
        .trim();

    return '''<?xml version="1.0" encoding="UTF-8"?>
<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="$lang">
    <voice name="$voiceName">
        <prosody rate="+5%" pitch="+2Hz" volume="+10%">
            $cleanText
        </prosody>
    </voice>
</speak>''';
  }

  String _resolveVoice(String languageCode) {
    return _edgeTtsVoices[languageCode] ?? _edgeTtsVoices['en']!;
  }

  String _resolveLocale(String voiceName) {
    if (voiceName.contains('hi-IN')) return 'hi-IN';
    if (voiceName.contains('en-IN')) return 'en-IN';
    if (voiceName.contains('gu-IN')) return 'gu-IN';
    if (voiceName.contains('mr-IN')) return 'mr-IN';
    if (voiceName.contains('ta-IN')) return 'ta-IN';
    if (voiceName.contains('te-IN')) return 'te-IN';
    if (voiceName.contains('bn-IN')) return 'bn-IN';
    if (voiceName.contains('pa-IN')) return 'pa-IN';
    if (voiceName.contains('kn-IN')) return 'kn-IN';
    if (voiceName.contains('ml-IN')) return 'ml-IN';
    if (voiceName.contains('or-IN')) return 'or-IN';
    return 'en-US';
  }

  void stop() {
    _isSpeaking = false;
    try {
      MethodChannel('com.example.snaper/audio').invokeMethod('stopAudio');
    } catch (e) {
      // Ignore
    }
  }

  void dispose() {
    stop();
  }
}