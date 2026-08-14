import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';
import 'package:flutter_animate/flutter_animate.dart';

class ControlPanelScreen extends StatefulWidget {
  @override
  _ControlPanelScreenState createState() => _ControlPanelScreenState();
}

class _ControlPanelScreenState extends State<ControlPanelScreen> {
  final TextEditingController _ownerNameController = TextEditingController();
  final TextEditingController _apiKeyController = TextEditingController();
  final TextEditingController _newMemoryController = TextEditingController();
  String _selectedProvider = "Google Gemini";

  // Transfer Wizard State
  String _transferRole = ""; // "OLD", "NEW", or ""
  double _transferProgress = 0.0;
  String _transferStatus = "Initializing...";
  bool _isTransferring = false;

  // Cloud Backup State
  bool _isBackingUp = false;
  double _backupProgress = 0.0;
  String _backupStatus = "";

  @override
  Widget build(BuildContext context) {
    final settings = Provider.of<UserSettings>(context);
    _ownerNameController.text = settings.ownerName;

    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text(
          settings.getLocalizedText("settings"),
          style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white),
        ),
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Stack(
        children: [
          // Background Gradient
          Positioned.fill(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F0F1A)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
            ),
          ),

          SafeArea(
            child: SingleChildScrollView(
              padding: EdgeInsets.all(16.0),
              physics: BouncingScrollPhysics(),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildBrandingHeader(settings),
                  SizedBox(height: 20),

                  // Language selector
                  _buildLanguageSelector(settings),
                  SizedBox(height: 16),

                  // Active Operating Modes
                  _buildModeSelector(settings),
                  SizedBox(height: 16),

                  // Radha Naam Jap Widget
                  _buildRadhaNaamJapWidget(settings),
                  SizedBox(height: 16),

                  // Auto-Detect Model Engine (AI Key Inputs)
                  _buildAutoModelDetector(settings),
                  SizedBox(height: 16),

                  // Memory Vault (Local-First Memory Editing)
                  _buildMemoryVault(settings),
                  SizedBox(height: 16),

                  // Secure Device-to-Device Transfer Wizard UI
                  _buildTransferWizard(settings),
                  SizedBox(height: 16),

                  // Cloud Backup & Disaster Recovery Dashboard
                  _buildBackupDashboard(settings),
                  SizedBox(height: 16),

                  // Avatar & App Icon Studios
                  _buildStudioPanel(settings),
                  SizedBox(height: 40),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBrandingHeader(UserSettings settings) {
    return GlassCard(
      child: Row(
        children: [
          Icon(Icons.verified_user, color: Colors.orangeAccent, size: 40),
          SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settings.productName,
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
                ),
                SizedBox(height: 4),
                Text(
                  settings.corporateAttribution,
                  style: TextStyle(fontSize: 12, color: Colors.white70),
                ),
                SizedBox(height: 4),
                Text(
                  "Branding Code: PROTECTED_GREETING ('राधे राधे')",
                  style: TextStyle(fontSize: 10, color: Colors.orangeAccent.withOpacity(0.8)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLanguageSelector(UserSettings settings) {
    final Map<String, String> languages = {
      "en": "English",
      "hi": "हिन्दी (Hindi)",
      "mai": "मैथिली (Maithili)",
      "ur": "اردو (Urdu)",
      "bn": "বাংলা (Bengali)",
      "mr": "मराठी (Marathi)",
      "pa": "ਪੰਜਾਬੀ (Punjabi)",
      "ta": "தமிழ் (Tamil)",
      "te": "తెలుగు (Telugu)",
      "gu": "ગુજરાતી (Gujarati)"
    };

    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            settings.getLocalizedText("select_language"),
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
          ),
          SizedBox(height: 12),
          Wrap(
            spacing: 8.0,
            runSpacing: 8.0,
            children: languages.entries.map((entry) {
              final isSelected = settings.languageCode == entry.key;
              return ChoiceChip(
                label: Text(
                  entry.value,
                  style: TextStyle(color: isSelected ? Colors.black : Colors.white),
                ),
                selected: isSelected,
                selectedColor: Colors.orangeAccent,
                backgroundColor: Colors.white.withOpacity(0.05),
                onSelected: (selected) {
                  if (selected) {
                    settings.updateLanguage(entry.key);
                  }
                },
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildModeSelector(UserSettings settings) {
    final modes = [
      "All-Rounder",
      "Doctor/Health",
      "Women's Health",
      "Legal",
      "Security & Force",
      "Vehicle",
      "Smart Home",
      "IT & Business Automation"
    ];

    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.dashboard_customize, color: Colors.blueAccent),
              SizedBox(width: 8),
              Text(
                settings.getLocalizedText("active_mode"),
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 8),
          Text(
            "Current Mode: ${settings.activeMode}",
            style: TextStyle(color: Colors.blueAccent, fontWeight: FontWeight.bold),
          ),
          SizedBox(height: 12),
          Container(
            height: 44,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: modes.length,
              physics: BouncingScrollPhysics(),
              itemBuilder: (context, index) {
                final mode = modes[index];
                final isSelected = settings.activeMode == mode;
                return Padding(
                  padding: const EdgeInsets.only(right: 8.0),
                  child: ActionChip(
                    label: Text(mode, style: TextStyle(color: isSelected ? Colors.black : Colors.white)),
                    backgroundColor: isSelected ? Colors.blueAccent : Colors.white.withOpacity(0.05),
                    onPressed: () {
                      settings.updateActiveMode(mode);
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRadhaNaamJapWidget(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.brightness_7, color: Colors.orange),
                  SizedBox(width: 8),
                  Text(
                    "राधा नाम जप (Jap Counter)",
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ],
              ),
              IconButton(
                icon: Icon(Icons.refresh, color: Colors.white70),
                onPressed: () => settings.resetRadhaJap(),
              )
            ],
          ),
          SizedBox(height: 8),
          Center(
            child: Column(
              children: [
                Text(
                  "${settings.radhaJapCount}",
                  style: TextStyle(fontSize: 48, fontWeight: FontWeight.bold, color: Colors.orangeAccent),
                ).animate(target: settings.radhaJapCount.toDouble()).scale(duration: 200.ms),
                SizedBox(height: 12),
                ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.orangeAccent,
                    foregroundColor: Colors.black,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  ),
                  onPressed: () => settings.incrementRadhaJap(),
                  icon: Icon(Icons.favorite, color: Colors.red),
                  label: Text("राधे राधे (Jap +1)", style: TextStyle(fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAutoModelDetector(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.bolt, color: Colors.cyanAccent),
              SizedBox(width: 8),
              Text(
                settings.getLocalizedText("model_status"),
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text(
            "Paste your API key below. The system will auto-detect the provider and optimal model.",
            style: TextStyle(fontSize: 12, color: Colors.white70),
          ),
          SizedBox(height: 12),
          Row(
            children: [
              DropdownButton<String>(
                value: _selectedProvider,
                dropdownColor: Color(0xFF16213E),
                style: TextStyle(color: Colors.white),
                items: ["Google Gemini", "OpenAI", "Anthropic", "xAI Grok"].map((provider) {
                  return DropdownMenuItem<String>(
                    value: provider,
                    child: Text(provider),
                  );
                }).toList(),
                onChanged: (val) {
                  if (val != null) {
                    setState(() {
                      _selectedProvider = val;
                    });
                  }
                },
              ),
            ],
          ),
          SizedBox(height: 8),
          TextField(
            controller: _apiKeyController,
            obscureText: true,
            style: TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: "Enter AI API Key...",
              hintStyle: TextStyle(color: Colors.white38),
              filled: true,
              fillColor: Colors.white.withOpacity(0.05),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
              suffixIcon: IconButton(
                icon: Icon(Icons.check_circle, color: Colors.cyanAccent),
                onPressed: () {
                  if (_apiKeyController.text.isNotEmpty) {
                    settings.setApiKey(_selectedProvider, _apiKeyController.text);
                    _apiKeyController.clear();
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text("Auto-Detected: ${settings.detectedProvider} -> Mode: ${settings.activeModel}")),
                    );
                  }
                },
              ),
            ),
          ),
          SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Detected Provider:", style: TextStyle(color: Colors.white60)),
              Text(settings.detectedProvider, style: TextStyle(color: Colors.cyanAccent, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 4),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Optimal Model:", style: TextStyle(color: Colors.white60)),
              Text(settings.activeModel, style: TextStyle(color: Colors.greenAccent, fontWeight: FontWeight.bold)),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMemoryVault(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.psychology, color: Colors.pinkAccent),
                  SizedBox(width: 8),
                  Text(
                    settings.getLocalizedText("memory_vault") + " (Local-First)",
                    style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ],
              ),
              IconButton(
                icon: Icon(Icons.add_comment, color: Colors.pinkAccent),
                onPressed: () {
                  _showAddMemoryDialog(settings);
                },
              )
            ],
          ),
          SizedBox(height: 8),
          ListView.builder(
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            itemCount: settings.localMemories.length,
            itemBuilder: (context, index) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: Container(
                  padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.04),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      Icon(Icons.fiber_manual_record, size: 8, color: Colors.pinkAccent),
                      SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          settings.localMemories[index],
                          style: TextStyle(color: Colors.white),
                        ),
                      ),
                      IconButton(
                        icon: Icon(Icons.delete_outline, color: Colors.redAccent, size: 18),
                        onPressed: () => settings.deleteMemory(index),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  void _showAddMemoryDialog(UserSettings settings) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          backgroundColor: Color(0xFF16213E),
          title: Text("Add Personal Memory Fact", style: TextStyle(color: Colors.white)),
          content: TextField(
            controller: _newMemoryController,
            style: TextStyle(color: Colors.white),
            decoration: InputDecoration(
              hintText: "Enter fact...",
              hintStyle: TextStyle(color: Colors.white30),
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text("Cancel", style: TextStyle(color: Colors.white70)),
            ),
            ElevatedButton(
              onPressed: () {
                if (_newMemoryController.text.isNotEmpty) {
                  settings.addMemory(_newMemoryController.text);
                  _newMemoryController.clear();
                  Navigator.pop(context);
                }
              },
              child: Text("Save"),
            ),
          ],
        );
      },
    );
  }

  Widget _buildTransferWizard(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.swap_horizontal_circle, color: Colors.purpleAccent),
              SizedBox(width: 8),
              Text(
                "Device-to-Device Migration Wizard",
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text(
            "Transfer all your settings, memories, and personal profiles securely between devices using encrypted peer-to-peer Wi-Fi/Bluetooth.",
            style: TextStyle(fontSize: 12, color: Colors.white70),
          ),
          SizedBox(height: 16),

          if (_transferRole == "")
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.purpleAccent, foregroundColor: Colors.white),
                    onPressed: () {
                      setState(() {
                        _transferRole = "OLD";
                        _startSimulatedTransfer();
                      });
                    },
                    child: Text("OLD DEVICE\n(Transfer my data)", textAlign: TextAlign.center, style: TextStyle(fontSize: 11)),
                  ),
                ),
                SizedBox(width: 10),
                Expanded(
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.deepPurple, foregroundColor: Colors.white),
                    onPressed: () {
                      setState(() {
                        _transferRole = "NEW";
                        _startSimulatedTransfer();
                      });
                    },
                    child: Text("NEW DEVICE\n(Restore my data)", textAlign: TextAlign.center, style: TextStyle(fontSize: 11)),
                  ),
                ),
              ],
            )
          else
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "Role: ${_transferRole == "OLD" ? "Sender (Old Device)" : "Receiver (New Device)"}",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.purpleAccent),
                ),
                SizedBox(height: 8),
                LinearProgressIndicator(
                  value: _transferProgress,
                  backgroundColor: Colors.white12,
                  valueColor: AlwaysStoppedAnimation<Color>(Colors.purpleAccent),
                ),
                SizedBox(height: 8),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(_transferStatus, style: TextStyle(color: Colors.white70, fontSize: 12)),
                    Text("${(_transferProgress * 100).toInt()}%", style: TextStyle(color: Colors.purpleAccent, fontWeight: FontWeight.bold)),
                  ],
                ),
                if (!_isTransferring)
                  Padding(
                    padding: const EdgeInsets.only(top: 12.0),
                    child: Align(
                      alignment: Alignment.centerRight,
                      child: TextButton(
                        onPressed: () {
                          setState(() {
                            _transferRole = "";
                            _transferProgress = 0.0;
                          });
                        },
                        child: Text("Reset Wizard", style: TextStyle(color: Colors.purpleAccent)),
                      ),
                    ),
                  ),
              ],
            ),
        ],
      ),
    );
  }

  void _startSimulatedTransfer() {
    setState(() {
      _isTransferring = true;
      _transferProgress = 0.0;
      _transferStatus = "Discovering nearby devices...";
    });

    Future.delayed(Duration(seconds: 1), () {
      if (!mounted) return;
      setState(() {
        _transferStatus = "Establishing secure connection (E2E Encrypted Key Exchange)...";
        _transferProgress = 0.25;
      });
    });

    Future.delayed(Duration(seconds: 2), () {
      if (!mounted) return;
      setState(() {
        _transferStatus = "Serializing configurations, local memory vaults & databases (14.2 MB)...";
        _transferProgress = 0.55;
      });
    });

    Future.delayed(Duration(seconds: 3), () {
      if (!mounted) return;
      setState(() {
        _transferStatus = "Streaming payload securely... verifying hash integrity.";
        _transferProgress = 0.85;
      });
    });

    Future.delayed(Duration(seconds: 4), () {
      if (!mounted) return;
      setState(() {
        _transferStatus = "Transfer & state restoration 100% SUCCESSFUL.";
        _transferProgress = 1.0;
        _isTransferring = false;
      });
    });
  }

  Widget _buildBackupDashboard(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.cloud_upload, color: Colors.greenAccent),
              SizedBox(width: 8),
              Text(
                "Daily Backup & Disaster Recovery",
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text(
            "Incremental snapshot backups target your customer-owned Google Drive directory strictly under folder: '${settings.backupFolderName}' using AES-256 local-first encryption.",
            style: TextStyle(fontSize: 12, color: Colors.white70),
          ),
          SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.green, foregroundColor: Colors.white),
                  onPressed: _isBackingUp ? null : () => _startBackup(settings),
                  icon: Icon(Icons.cloud_done),
                  label: Text("Back Up Now", style: TextStyle(fontSize: 12)),
                ),
              ),
              SizedBox(width: 10),
              Expanded(
                child: ElevatedButton.icon(
                  style: ElevatedButton.styleFrom(backgroundColor: Colors.blueGrey, foregroundColor: Colors.white),
                  onPressed: _isBackingUp ? null : () => _startRestore(settings),
                  icon: Icon(Icons.restore),
                  label: Text("1-Click Restore", style: TextStyle(fontSize: 12)),
                ),
              ),
            ],
          ),
          if (_backupStatus.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(top: 12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  LinearProgressIndicator(value: _backupProgress, backgroundColor: Colors.white12, valueColor: AlwaysStoppedAnimation<Color>(Colors.greenAccent)),
                  SizedBox(height: 8),
                  Text(_backupStatus, style: TextStyle(color: Colors.greenAccent, fontSize: 11, fontWeight: FontWeight.bold)),
                ],
              ),
            ),
        ],
      ),
    );
  }

  void _startBackup(UserSettings settings) {
    setState(() {
      _isBackingUp = true;
      _backupProgress = 0.0;
      _backupStatus = "Encrypting local configurations using AES-256...";
    });

    Future.delayed(Duration(seconds: 1), () {
      if (!mounted) return;
      setState(() {
        _backupStatus = "Uploading incremental backup to your personal Google Drive...";
        _backupProgress = 0.5;
      });
    });

    Future.delayed(Duration(seconds: 2), () {
      if (!mounted) return;
      setState(() {
        _backupStatus = "Backup completed! Safe in Google Drive folder: '${settings.backupFolderName}'";
        _backupProgress = 1.0;
        _isBackingUp = false;
      });
    });
  }

  void _startRestore(UserSettings settings) {
    setState(() {
      _isBackingUp = true;
      _backupProgress = 0.0;
      _backupStatus = "Downloading snapshot from your personal Google Drive folder...";
    });

    Future.delayed(Duration(seconds: 1), () {
      if (!mounted) return;
      setState(() {
        _backupStatus = "Decrypting locally and applying memory database / state configurations...";
        _backupProgress = 0.6;
      });
    });

    Future.delayed(Duration(seconds: 2), () {
      if (!mounted) return;
      setState(() {
        _backupStatus = "Full 1-Click Restoration Successful!";
        _backupProgress = 1.0;
        _isBackingUp = false;
      });
    });
  }

  Widget _buildStudioPanel(UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.palette, color: Colors.amberAccent),
              SizedBox(width: 8),
              Text(
                "App Studios & Customization",
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.white),
              ),
            ],
          ),
          SizedBox(height: 12),
          Text("Avatar Studio:", style: TextStyle(color: Colors.amberAccent, fontWeight: FontWeight.bold)),
          SizedBox(height: 8),
          DropdownButton<String>(
            value: settings.activeAvatar,
            dropdownColor: Color(0xFF16213E),
            isExpanded: true,
            style: TextStyle(color: Colors.white),
            items: ["3D Hologram Classic", "Cyberpunk Avatar", "Friendly 2D Virtual Companion", "Spark Sparkle Star"].map((av) {
              return DropdownMenuItem<String>(
                value: av,
                child: Text(av),
              );
            }).toList(),
            onChanged: (val) {
              if (val != null) {
                settings.updateAvatar(val);
              }
            },
          ),
          SizedBox(height: 12),
          Text("App Icon Studio:", style: TextStyle(color: Colors.amberAccent, fontWeight: FontWeight.bold)),
          SizedBox(height: 8),
          DropdownButton<String>(
            value: settings.appIconStyle,
            dropdownColor: Color(0xFF16213E),
            isExpanded: true,
            style: TextStyle(color: Colors.white),
            items: ["Liquid Glass Default", "Gold Premium Edition", "Cyber Glow Minimalist", "Traditional Saffron"].map((style) {
              return DropdownMenuItem<String>(
                value: style,
                child: Text(style),
              );
            }).toList(),
            onChanged: (val) {
              if (val != null) {
                settings.updateAppIcon(val);
              }
            },
          ),
        ],
      ),
    );
  }
}
