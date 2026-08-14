import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:snaper_ai_assistant/data/preferences/user_settings.dart';
import 'package:snaper_ai_assistant/ui/glass/glass_components.dart';
import 'package:snaper_ai_assistant/services/python_process_bridge.dart';

class ControlCenterScreen extends StatefulWidget {
  @override
  _ControlCenterScreenState createState() => _ControlCenterScreenState();
}

class _ControlCenterScreenState extends State<ControlCenterScreen> {
  final PythonProcessBridge _bridge = PythonProcessBridge();
  
  // Company Branding Inputs
  final TextEditingController _agencyNameController = TextEditingController(text: "Snaper AI Agency");
  final TextEditingController _taglineController = TextEditingController(text: "COO & Autonomous Software Factory");
  final TextEditingController _domainController = TextEditingController(text: "https://snaper.ai");

  // CRM Lead Input fields
  final TextEditingController _leadNameController = TextEditingController();
  final TextEditingController _leadCompanyController = TextEditingController();
  final TextEditingController _leadBudgetController = TextEditingController();

  // Dynamic Custom Platform Inputs
  final TextEditingController _customPlatformNameController = TextEditingController();
  final TextEditingController _customPlatformUrlController = TextEditingController();

  // Advanced Media & Bhakti Mode State
  final TextEditingController _gallerySearchController = TextEditingController();
  List<dynamic> _galleryResults = [];
  String _nowPlaying = "Saffron Devotional Beats";
  int _bhaktiIgnoreCount = 0;

  // Child Safety & IoT State
  bool _isChildDetected = false;
  List<dynamic> _iotDevices = [];

  // MODULE 29, 30 & 31: Advanced Security Framework State
  Map<String, dynamic> _securityStatus = {};
  List<dynamic> _securityLogs = [];

  // MODULE 33, 34 & 35 State
  Map<String, dynamic> _environmentSensing = {};
  bool _isAcousticFinderRunning = false;

  List<dynamic> _leads = [
    {"name": "Siddharth Sharma", "company": "Mumbai FinTech", "status": "Negotiation", "budget": 150000.0, "notes": "Highly interested in Automated Lead Gen"},
    {"name": "Rohan Deshmukh", "company": "Delhi Logistics", "status": "Payment", "budget": 200000.0, "notes": "Awaiting final GST invoice"},
    {"name": "Ananya Sen", "company": "Bangalore EdTech", "status": "Success", "budget": 120000.0, "notes": "Delivered MVP on time!"}
  ];

  List<dynamic> _invoices = [];
  List<dynamic> _customPlatforms = [];

  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _fetchEngineState();
  }

  Future<void> _triggerAcousticFinder() async {
    final settings = Provider.of<UserSettings>(context, listen: false);
    setState(() => _isAcousticFinderRunning = true);
    if (_bridge.isConnected) {
      final resp = await _bridge.sendRequest("trigger_phone_finder");
      if (resp["status"] == "success") {
        settings.updateAssistantState(AssistantState.EMERGENCY);
        settings.updateLastResponseText(resp["response"]);
      }
    }
    Future.delayed(Duration(seconds: 10), () {
      if (mounted) setState(() => _isAcousticFinderRunning = false);
    });
  }

  Future<void> _detectEnvironment() async {
    if (_bridge.isConnected) {
       final resp = await _bridge.sendRequest("detect_environment_sensing", {
         "light_lux": 1.5,
         "proximity_near": true,
         "orientation": "Face Up"
       });
       if (resp["status"] == "success") {
         setState(() {
           _environmentSensing = resp;
         });
         ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Environment Detected: ${resp['environment']}")));
       }
    }
  }

  Future<void> _fetchEngineState() async {
    if (!_bridge.isConnected) return;
    setState(() => _isLoading = true);
    try {
      final leadResp = await _bridge.sendRequest("get_leads");
      if (leadResp["status"] == "success" && leadResp["leads"] != null) {
        setState(() {
          _leads = leadResp["leads"];
        });
      }
      final invResp = await _bridge.sendRequest("get_invoices");
      if (invResp["status"] == "success" && invResp["invoices"] != null) {
        setState(() {
          _invoices = invResp["invoices"];
        });
      }
      final iotResp = await _bridge.sendRequest("get_iot_state");
      if (iotResp["status"] == "success") {
        setState(() {
          _iotDevices = iotResp["devices"];
        });
      }
      final secResp = await _bridge.sendRequest("get_security_status");
      if (secResp["status"] == "success") {
        setState(() {
          _securityStatus = secResp["security_status"] ?? {};
          _securityLogs = secResp["security_logs"] ?? [];
        });
      }
    } catch (e) {
      debugPrint("Error loading engine state: $e");
    } finally {
      setState(() => _isLoading = false);
    }
  }

  Future<void> _addNewLead() async {
    if (_leadNameController.text.isEmpty) return;
    final budget = double.tryParse(_leadBudgetController.text) ?? 50000.0;
    
    if (_bridge.isConnected) {
      await _bridge.sendRequest("add_lead", {
        "name": _leadNameController.text,
        "company": _leadCompanyController.text,
        "budget": budget,
        "status": "Lead",
        "notes": "Added from Agency Hub UI"
      });
      await _fetchEngineState();
    } else {
      // Local UI fallback
      setState(() {
        _leads.insert(0, {
          "name": _leadNameController.text,
          "company": _leadCompanyController.text,
          "status": "Lead",
          "budget": budget,
          "notes": "Added locally (Offline fallback)"
        });
      });
    }

    _leadNameController.clear();
    _leadCompanyController.clear();
    _leadBudgetController.clear();
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("New Lead added successfully!")));
  }

  Future<void> _generateUpiInvoice(String client, double amount) async {
    if (_bridge.isConnected) {
      final resp = await _bridge.sendRequest("generate_upi_qr", {
        "client_name": client,
        "amount": amount
      });
      if (resp["status"] == "success") {
        await _fetchEngineState();
        _showInvoiceDialog(resp["invoice_id"], resp["upi_deep_link"]);
      }
    } else {
      // Offline fallback deep link
      final fakeInvId = "INV-${DateTime.now().millisecondsSinceEpoch % 10000}";
      final fallbackUrl = "upi://pay?pa=sanjiv@okaxis&pn=Snaper_AI_Agency&am=$amount&tn=$fakeInvId&cu=INR";
      _showInvoiceDialog(fakeInvId, fallbackUrl);
    }
  }

  void _showInvoiceDialog(String invoiceId, String deepLink) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Color(0xFF1E1E2F),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: Text("Dynamic UPI Invoice", style: TextStyle(color: Colors.orangeAccent)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("Invoice ID: $invoiceId", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
            SizedBox(height: 12),
            Text("Deep Link Protocol:", style: TextStyle(color: Colors.white70, fontSize: 12)),
            Container(
              padding: EdgeInsets.all(8),
              decoration: BoxDecoration(color: Colors.black38, borderRadius: BorderRadius.circular(8)),
              child: Text(deepLink, style: TextStyle(color: Colors.blueAccent, fontSize: 10, fontFamily: 'monospace')),
            ),
            SizedBox(height: 16),
            Text("Simulating QR Generation with Standard Protocol. Securely monitored via Payment Gateways.", style: TextStyle(color: Colors.white60, fontSize: 11)),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text("Close", style: TextStyle(color: Colors.white70)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
            onPressed: () async {
              Navigator.pop(context);
              if (_bridge.isConnected) {
                final verifyResp = await _bridge.sendRequest("verify_payment_webhook", {
                  "invoice_id": invoiceId
                });
                if (verifyResp["status"] == "success") {
                  await _fetchEngineState();
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(verifyResp["message"])));
                }
              } else {
                ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Verified payment. GST Invoice sent to client!")));
              }
            },
            child: Text("Simulate Paid (Webhook)"),
          )
        ],
      ),
    );
  }

  Future<void> _addCustomSocialHub() async {
    if (_customPlatformNameController.text.isEmpty || _customPlatformUrlController.text.isEmpty) return;
    
    if (_bridge.isConnected) {
      final resp = await _bridge.sendRequest("add_custom_platform", {
        "platform_name": _customPlatformNameController.text,
        "api_url": _customPlatformUrlController.text
      });
      if (resp["status"] == "success") {
        setState(() {
          _customPlatforms = resp["platforms"];
        });
      }
    } else {
      setState(() {
        _customPlatforms.add({
          "name": _customPlatformNameController.text,
          "url": _customPlatformUrlController.text
        });
      });
    }

    _customPlatformNameController.clear();
    _customPlatformUrlController.clear();
    Navigator.pop(context);
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Custom Social Platform integrated!")));
  }

  @override
  Widget build(BuildContext context) {
    final settings = Provider.of<UserSettings>(context);
    final width = MediaQuery.of(context).size.width;

    return Scaffold(
      backgroundColor: Color(0xFF0F0F1A),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        title: Text("Agency Command Center", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.white)),
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
              padding: EdgeInsets.symmetric(horizontal: width * 0.04, vertical: 12),
              physics: BouncingScrollPhysics(),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  
                  // Brand Header Configuration
                  _buildBrandingSection(width),
                  SizedBox(height: 16),

                  // KPI Target Board
                  _buildKpiProgressBoard(width),
                  SizedBox(height: 16),

                  // Lead Tracker CRM Dashboard
                  _buildCrmLeadTracker(width),
                  SizedBox(height: 16),

                  // Integrated Social Media Platform Hub
                  _buildSocialMediaPlatformHub(width),
                  SizedBox(height: 16),

                  // Advanced Media & Gallery Search
                  _buildGallerySearchSection(width),
                  SizedBox(height: 16),

                  // System Media & App Store Controller
                  _buildMediaControlSection(width),
                  SizedBox(height: 16),

                  // Bhakti Mode & Radha Nam Jap Engine
                  _buildBhaktiModeSection(width, settings),
                  SizedBox(height: 16),

                  // Child Safety & Exam Protocol
                  _buildChildSafetySection(width),
                  SizedBox(height: 16),

                  // Advanced Smart Home Control
                  _buildSmartHomeSection(width),
                  SizedBox(height: 16),

                  // Autonomous Security Section
                  _buildAutonomousSecuritySection(width),
                  SizedBox(height: 16),

                  // MODULE 33, 34 & 35: Device Finder & Connectivity
                  _buildAcousticFinderAndConnectivity(width, settings),
                  SizedBox(height: 16),

                  // Invoices List
                  _buildInvoicesSection(width),
                  SizedBox(height: 24),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _simulateGyroTouch() async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("simulate_gyro_touch");
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Future<void> _simulateFaceCheck(bool isOwner) async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("simulate_post_unlock_face_check", {"face_match_success": isOwner});
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Future<void> _simulateHomeIntrusion() async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("simulate_home_intrusion");
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Future<void> _simulateVehicleTamper() async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("simulate_vehicle_tamper");
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Future<void> _simulateVehicleEngineCutoff() async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("simulate_vehicle_unauthorized_drive");
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Future<void> _resolveSecurityBiometric() async {
    if (!_bridge.isConnected) return;
    final resp = await _bridge.sendRequest("resolve_biometric_auth");
    if (resp["status"] == "success") {
      await _fetchEngineState();
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
    }
  }

  Widget _buildAutonomousSecuritySection(double width) {
    final status = _securityStatus;
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.security, color: Colors.redAccent, size: 24),
                  SizedBox(width: 8),
                  Text("S96 Advanced Security Framework", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                ],
              ),
              if (status["device_locked"] == true || status["engine_immobilized"] == true)
                IconButton(
                  icon: Icon(Icons.fingerprint, color: Colors.greenAccent, size: 28),
                  tooltip: "Biometric Authorize (Unlock)",
                  onPressed: _resolveSecurityBiometric,
                )
            ],
          ),
          SizedBox(height: 12),
          
          // Status Grid
          GridView.count(
            crossAxisCount: 2,
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            childAspectRatio: 2.8,
            crossAxisSpacing: 8,
            mainAxisSpacing: 8,
            children: [
              _buildStatusIndicator("Device Status", status["device_locked"] == true ? "OVERLAY LOCKED 🔒" : "UNLOCKED 🔓", status["device_locked"] == true ? Colors.redAccent : Colors.greenAccent),
              _buildStatusIndicator("Engine CAN-bus", status["engine_immobilized"] == true ? "IMMOBILIZED 🚫" : "NORMAL ✅", status["engine_immobilized"] == true ? Colors.redAccent : Colors.greenAccent),
              _buildStatusIndicator("Siren Alert", status["siren_active"] == true ? "ACTIVE 🚨" : "OFF 🔇", status["siren_active"] == true ? Colors.redAccent : Colors.white30),
              _buildStatusIndicator("CCTV Intercept", status["cctv_blocked_and_trapped"] == true ? "TRAPPED ⚡" : "MONITORING 📹", status["cctv_blocked_and_trapped"] == true ? Colors.orangeAccent : Colors.white30),
            ],
          ),
          SizedBox(height: 16),

          if (status["last_captured_photo"] != null) ...[
            Container(
              padding: EdgeInsets.all(10),
              margin: EdgeInsets.only(bottom: 12),
              decoration: BoxDecoration(color: Colors.red.withOpacity(0.1), borderRadius: BorderRadius.circular(10), border: Border.all(color: Colors.red.withOpacity(0.3))),
              child: Row(
                children: [
                  Icon(Icons.photo_camera, color: Colors.redAccent),
                  SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text("Intruder Snap Captured!", style: TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold)),
                        Text("File: ${status['last_captured_photo']}", style: TextStyle(color: Colors.white70, fontSize: 10)),
                        Text("Logged: ${status['last_captured_timestamp']}", style: TextStyle(color: Colors.white54, fontSize: 9)),
                      ],
                    ),
                  )
                ],
              ),
            ),
          ],

          Text("Interactive Simulations:", style: TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.bold)),
          SizedBox(height: 8),

          // Simulation buttons
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              ActionChip(
                avatar: Icon(Icons.screen_rotation, size: 16, color: Colors.orangeAccent),
                label: Text("Gyro Pickup", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: _simulateGyroTouch,
              ),
              ActionChip(
                avatar: Icon(Icons.face_retouching_natural, size: 16, color: Colors.greenAccent),
                label: Text("Face Match Success", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: () => _simulateFaceCheck(true),
              ),
              ActionChip(
                avatar: Icon(Icons.no_accounts, size: 16, color: Colors.redAccent),
                label: Text("Face Match Fail (Lock)", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: () => _simulateFaceCheck(false),
              ),
              ActionChip(
                avatar: Icon(Icons.home_outlined, size: 16, color: Colors.redAccent),
                label: Text("Home Intrusion (112)", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: _simulateHomeIntrusion,
              ),
              ActionChip(
                avatar: Icon(Icons.directions_car, size: 16, color: Colors.orangeAccent),
                label: Text("Vandalism Warning", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: _simulateVehicleTamper,
              ),
              ActionChip(
                avatar: Icon(Icons.key_off, size: 16, color: Colors.redAccent),
                label: Text("Engine Ignition Cutoff", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: _simulateVehicleEngineCutoff,
              ),
            ],
          ),
          SizedBox(height: 16),

          Text("Real-Time Security Audit Logs:", style: TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.bold)),
          SizedBox(height: 8),

          // Security Logs List
          Container(
            height: 180,
            decoration: BoxDecoration(color: Colors.black26, borderRadius: BorderRadius.circular(10)),
            child: _securityLogs.isEmpty
                ? Center(child: Padding(padding: EdgeInsets.all(12), child: Text("No security logs available.", style: TextStyle(color: Colors.white30, fontSize: 12))))
                : ListView.builder(
                    shrinkWrap: true,
                    physics: ClampingScrollPhysics(),
                    itemCount: _securityLogs.length,
                    itemBuilder: (context, index) {
                      final log = _securityLogs[index];
                      Color severityColor = Colors.white54;
                      if (log["severity"] == "CRITICAL") severityColor = Colors.redAccent;
                      if (log["severity"] == "HIGH") severityColor = Colors.orangeAccent;
                      if (log["severity"] == "WARNING") severityColor = Colors.amberAccent;
                      if (log["severity"] == "INFO") severityColor = Colors.blueAccent;
                      return Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6.0),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              width: 6,
                              height: 6,
                              margin: EdgeInsets.only(top: 5, right: 8),
                              decoration: BoxDecoration(color: severityColor, shape: BoxShape.circle),
                            ),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(log["event"] ?? "", style: TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.w500)),
                                  SizedBox(height: 2),
                                  Text(log["timestamp"] ?? "", style: TextStyle(color: Colors.white30, fontSize: 9)),
                                ],
                              ),
                            )
                          ],
                        ),
                      );
                    },
                  ),
          )
        ],
      ),
    );
  }

  Widget _buildStatusIndicator(String label, String val, Color valColor) {
    return Container(
      padding: EdgeInsets.all(6),
      decoration: BoxDecoration(color: Colors.white.withOpacity(0.02), borderRadius: BorderRadius.circular(8)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(label, style: TextStyle(color: Colors.white38, fontSize: 9)),
          SizedBox(height: 2),
          Text(val, style: TextStyle(color: valColor, fontSize: 11, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  Widget _buildAcousticFinderAndConnectivity(double width, UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.track_changes, color: Colors.greenAccent),
              SizedBox(width: 8),
              Text("Device Locator & Connectivity", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 12),
          
          // Acoustic Finder Trigger
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Acoustic Phone Finder", style: TextStyle(color: Colors.white, fontSize: 13)),
              ElevatedButton.icon(
                icon: Icon(Icons.record_voice_over, size: 16),
                label: Text(_isAcousticFinderRunning ? "Running..." : "Test Acoustic Trigger"),
                onPressed: _isAcousticFinderRunning ? null : _triggerAcousticFinder,
                style: ElevatedButton.styleFrom(backgroundColor: Colors.blueAccent, padding: EdgeInsets.symmetric(horizontal: 12)),
              ),
            ],
          ),
          SizedBox(height: 10),

          // Environment Sensing
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Environment Sensing", style: TextStyle(color: Colors.white, fontSize: 13)),
              TextButton.icon(
                icon: Icon(Icons.camera, size: 16, color: Colors.greenAccent),
                label: Text("Run Sensing", style: TextStyle(color: Colors.greenAccent)),
                onPressed: _detectEnvironment,
              ),
            ],
          ),
          if (_environmentSensing.isNotEmpty) ...[
            Container(
               padding: EdgeInsets.all(10),
               margin: EdgeInsets.only(top: 8),
               decoration: BoxDecoration(color: Colors.black26, borderRadius: BorderRadius.circular(10)),
               child: Column(
                 crossAxisAlignment: CrossAxisAlignment.start,
                 children: [
                    Text("📍 ${_environmentSensing['environment']}", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12)),
                    Text("${_environmentSensing['details']}", style: TextStyle(color: Colors.white70, fontSize: 10)),
                    SizedBox(height: 4),
                    Text("Speech: \"${_environmentSensing['hindi_alert']}\"", style: TextStyle(color: Colors.greenAccent, fontSize: 10, fontStyle: FontStyle.italic)),
                 ],
               ),
            ),
          ],
          SizedBox(height: 12),
          Divider(color: Colors.white12),

          // Connectivity & Addressing Toggles
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                   Text("Flight Mode (Local-First Engine)", style: TextStyle(color: Colors.white, fontSize: 13)),
                   Text(settings.isOnline ? "Online (Hybrid Router)" : "Offline (On-Device Processing)", style: TextStyle(color: Colors.white38, fontSize: 10)),
                ],
              ),
              Switch(
                value: !settings.isOnline,
                activeColor: Colors.orangeAccent,
                onChanged: (val) {
                  settings.toggleOnlineMode(!val);
                },
              ),
            ],
          ),
          SizedBox(height: 10),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                   Text("Allow 'Bhai' / 'Bro' (Strict Address)", style: TextStyle(color: Colors.white, fontSize: 13)),
                   Text(settings.isExplicitBhaiAllowed ? "Explicitly Allowed" : "Absolute Ban Active", style: TextStyle(color: Colors.redAccent, fontSize: 10)),
                ],
              ),
              Switch(
                value: settings.isExplicitBhaiAllowed,
                activeColor: Colors.redAccent,
                onChanged: (val) {
                  settings.toggleExplicitBhai(val);
                },
              ),
            ],
          ),
        ],
      ),
    );
  }
}

  Widget _buildBrandingSection(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.business_center, color: Colors.orangeAccent, size: 28),
              SizedBox(width: 8),
              Text("Company Profile & Branding Engine", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 12),
          _buildHubTextField("Agency Name", _agencyNameController),
          SizedBox(height: 8),
          _buildHubTextField("Tagline / Role", _taglineController),
          SizedBox(height: 8),
          _buildHubTextField("Official Web Domain", _domainController),
        ],
      ),
    );
  }

  Widget _buildKpiProgressBoard(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Operations & KPI Targets", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              Icon(Icons.trending_up, color: Colors.greenAccent),
            ],
          ),
          SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Monthly Target: ₹5,00,000", style: TextStyle(color: Colors.white70, fontSize: 13)),
              Text("Current: ₹1,20,000", style: TextStyle(color: Colors.greenAccent, fontSize: 13, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(10),
            child: LinearProgressIndicator(
              value: 120000 / 500000,
              minHeight: 10,
              backgroundColor: Colors.white10,
              color: Colors.greenAccent,
            ),
          ),
          SizedBox(height: 10),
          Text("COO Status: \"Active lead pipelines are being monitored perpetually. Auto-conversions in progress.\"", style: TextStyle(color: Colors.white60, fontSize: 11, fontStyle: FontStyle.italic)),
        ],
      ),
    );
  }

  Widget _buildCrmLeadTracker(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.groups, color: Colors.blueAccent, size: 24),
                  SizedBox(width: 8),
                  Text("Built-In Advanced CRM", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                ],
              ),
              IconButton(
                icon: Icon(Icons.add, color: Colors.blueAccent),
                onPressed: () => _showAddLeadDialog(),
              )
            ],
          ),
          SizedBox(height: 8),
          ListView.separated(
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            itemCount: _leads.length,
            separatorBuilder: (_, __) => Divider(color: Colors.white12),
            itemBuilder: (context, index) {
              final lead = _leads[index];
              return Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(lead["name"] ?? "Anonymous", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14)),
                        Text("${lead["company"] ?? "Freelance"} • Budget: ₹${lead["budget"] ?? 0.0}", style: TextStyle(color: Colors.white60, fontSize: 11)),
                        Text("Notes: ${lead["notes"] ?? ''}", style: TextStyle(color: Colors.white38, fontSize: 10), overflow: TextOverflow.ellipsis),
                      ],
                    ),
                  ),
                  Row(
                    children: [
                      Container(
                        padding: EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: _getStatusColor(lead["status"]).withOpacity(0.2),
                          border: Border.all(color: _getStatusColor(lead["status"]), width: 1),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(
                          lead["status"] ?? "Lead",
                          style: TextStyle(color: _getStatusColor(lead["status"]), fontSize: 10, fontWeight: FontWeight.bold),
                        ),
                      ),
                      SizedBox(width: 6),
                      if (lead["status"] != "Success" && lead["status"] != "Paid")
                        GestureDetector(
                          onTap: () => _generateUpiInvoice(lead["name"], lead["budget"]),
                          child: Container(
                            padding: EdgeInsets.all(6),
                            decoration: BoxDecoration(color: Colors.orangeAccent.withOpacity(0.15), shape: BoxShape.circle),
                            child: Icon(Icons.qr_code, color: Colors.orangeAccent, size: 16),
                          ),
                        )
                    ],
                  ),
                ],
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildSocialMediaPlatformHub(double width) {
    final activePlatforms = [
      {"name": "Instagram DM", "icon": Icons.camera_alt, "status": "Auto-Reply Active"},
      {"name": "YouTube Analytics", "icon": Icons.video_library, "status": "Monitoring"},
      {"name": "LinkedIn Leads", "icon": Icons.work, "status": "Prospecting active"},
      {"name": "Facebook Pages", "icon": Icons.pages, "status": "Connected"},
      {"name": "Twitter / X", "icon": Icons.alternate_email, "status": "DM Handling Active"}
    ];

    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("All-In-One Social Platform Hub", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              IconButton(
                icon: Icon(Icons.add_link, color: Colors.orangeAccent),
                onPressed: () => _showAddPlatformDialog(),
              ),
            ],
          ),
          SizedBox(height: 12),
          ...activePlatforms.map((p) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 6.0),
            child: Row(
              children: [
                Icon(p["icon"] as IconData, color: Colors.orangeAccent, size: 20),
                SizedBox(width: 12),
                Expanded(
                  child: Text(p["name"] as String, style: TextStyle(color: Colors.white, fontWeight: FontWeight.w500, fontSize: 13)),
                ),
                Text(p["status"] as String, style: TextStyle(color: Colors.greenAccent, fontSize: 11)),
              ],
            ),
          )).toList(),
          if (_customPlatforms.isNotEmpty) ...[
            Divider(color: Colors.white12),
            ..._customPlatforms.map((p) => Padding(
              padding: const EdgeInsets.symmetric(vertical: 6.0),
              child: Row(
                children: [
                  Icon(Icons.link, color: Colors.blueAccent, size: 20),
                  SizedBox(width: 12),
                  Expanded(
                    child: Text(p["name"], style: TextStyle(color: Colors.white, fontWeight: FontWeight.w500, fontSize: 13)),
                  ),
                  Text("Custom Port Bound", style: TextStyle(color: Colors.blueAccent, fontSize: 11)),
                ],
              ),
            )).toList(),
          ]
        ],
      ),
    );
  }

  Widget _buildInvoicesSection(double width) {
    if (_invoices.isEmpty) return SizedBox.shrink();
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text("UPI Invoice History", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
          SizedBox(height: 10),
          ..._invoices.map((inv) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 6.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(inv["invoice_id"], style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                    Text(inv["client_name"], style: TextStyle(color: Colors.white60, fontSize: 11)),
                  ],
                ),
                Text(
                  "₹${inv['amount']} • ${inv['status']}",
                  style: TextStyle(
                    color: inv["status"] == "Paid" ? Colors.greenAccent : Colors.orangeAccent,
                    fontWeight: FontWeight.bold,
                    fontSize: 12
                  ),
                ),
              ],
            ),
          )).toList()
        ],
      ),
    );
  }

  Widget _buildHubTextField(String label, TextEditingController ctrl) {
    return Container(
      margin: EdgeInsets.only(bottom: 6),
      height: 42,
      child: TextField(
        controller: ctrl,
        style: TextStyle(color: Colors.white, fontSize: 13),
        decoration: InputDecoration(
          labelText: label,
          labelStyle: TextStyle(color: Colors.white38, fontSize: 12),
          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          enabledBorder: OutlineInputBorder(borderSide: BorderSide(color: Colors.white12), borderRadius: BorderRadius.circular(10)),
          focusedBorder: OutlineInputBorder(borderSide: BorderSide(color: Colors.orangeAccent), borderRadius: BorderRadius.circular(10)),
        ),
      ),
    );
  }

  Color _getStatusColor(String? status) {
    switch (status) {
      case "Success":
      case "Paid":
        return Colors.greenAccent;
      case "Payment":
      case "Negotiation":
        return Colors.orangeAccent;
      case "Discovery":
        return Colors.blueAccent;
      default:
        return Colors.white54;
    }
  }

  void _showAddLeadDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Color(0xFF16213E),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
        title: Text("Add CRM Lead", style: TextStyle(color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildHubTextField("Client Name", _leadNameController),
            SizedBox(height: 8),
            _buildHubTextField("Company", _leadCompanyController),
            SizedBox(height: 8),
            _buildHubTextField("Project Budget (INR)", _leadBudgetController),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text("Cancel", style: TextStyle(color: Colors.white70)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.orangeAccent),
            onPressed: _addNewLead,
            child: Text("Save Lead"),
          )
        ],
      ),
    );
  }

  void _showAddPlatformDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Color(0xFF16213E),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
        title: Text("Add Custom Platform Hub", style: TextStyle(color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            _buildHubTextField("Platform Name", _customPlatformNameController),
            SizedBox(height: 8),
            _buildHubTextField("Web Domain / Webhook Endpoint", _customPlatformUrlController),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: Text("Cancel", style: TextStyle(color: Colors.white70)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.orangeAccent),
            onPressed: _addCustomSocialHub,
            child: Text("Integrate Platform"),
          )
        ],
      ),
    );
  }

  Widget _buildGallerySearchSection(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.photo_library, color: Colors.purpleAccent),
              SizedBox(width: 8),
              Text("Advanced Media Search", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 12),
          TextField(
            controller: _gallerySearchController,
            style: TextStyle(color: Colors.white, fontSize: 13),
            decoration: InputDecoration(
              hintText: "मम्मी का 5 दिन पुराना फोटो दिखाओ...",
              hintStyle: TextStyle(color: Colors.white24),
              suffixIcon: IconButton(
                icon: Icon(Icons.search, color: Colors.purpleAccent),
                onPressed: () async {
                  if (_bridge.isConnected) {
                    final resp = await _bridge.sendRequest("search_media_gallery", {"query": _gallerySearchController.text});
                    if (resp["status"] == "success") {
                      setState(() {
                        _galleryResults = resp["matches"];
                      });
                    }
                  }
                },
              ),
              filled: true,
              fillColor: Colors.white.withOpacity(0.05),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12), borderSide: BorderSide.none),
            ),
          ),
          if (_galleryResults.isNotEmpty) ...[
            SizedBox(height: 12),
            Container(
              height: 140,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: _galleryResults.length,
                itemBuilder: (context, index) {
                  final item = _galleryResults[index];
                  return Container(
                    width: 120,
                    margin: EdgeInsets.only(right: 10),
                    decoration: BoxDecoration(
                      color: Colors.white10,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: Colors.purpleAccent.withOpacity(0.3)),
                    ),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(item["type"] == "photo" ? Icons.image : Icons.videocam, color: Colors.purpleAccent),
                        SizedBox(height: 8),
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 4.0),
                          child: Text(item["filename"], style: TextStyle(color: Colors.white, fontSize: 9), textAlign: TextAlign.center, maxLines: 2),
                        ),
                        Text(item["date"].toString().split(' ')[0], style: TextStyle(color: Colors.white54, fontSize: 8)),
                      ],
                    ),
                  );
                },
              ),
            ),
          ]
        ],
      ),
    );
  }

  Widget _buildMediaControlSection(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("System Media & App Stores", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              Icon(Icons.play_circle_filled, color: Colors.redAccent),
            ],
          ),
          SizedBox(height: 12),
          Container(
            padding: EdgeInsets.all(12),
            decoration: BoxDecoration(color: Colors.black45, borderRadius: BorderRadius.circular(15)),
            child: Row(
              children: [
                Icon(Icons.music_note, color: Colors.redAccent),
                SizedBox(width: 12),
                Expanded(child: Text(_nowPlaying, style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.bold))),
                IconButton(icon: Icon(Icons.skip_previous, color: Colors.white), onPressed: () {}),
                IconButton(icon: Icon(Icons.play_arrow, color: Colors.white), onPressed: () {}),
                IconButton(icon: Icon(Icons.skip_next, color: Colors.white), onPressed: () {}),
              ],
            ),
          ),
          SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: GlossyButton(
                  label: "Install YouTube",
                  onPressed: () async {
                    if (_bridge.isConnected) {
                      final resp = await _bridge.sendRequest("control_system_media", {"control_type": "install_app", "target_value": "YouTube", "store_name": "Google Play"});
                      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["message"])));
                    }
                  },
                  icon: Icons.download,
                  gradientColors: [Colors.redAccent, Colors.deepOrange],
                ),
              ),
            ],
          )
        ],
      ),
    );
  }

  Widget _buildBhaktiModeSection(double width, UserSettings settings) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Icon(Icons.brightness_high, color: Colors.orange),
                  SizedBox(width: 8),
                  Text("Bhakti Mode & Radha Jap", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
                ],
              ),
              Switch(
                value: settings.bhaktiModeEnabled,
                activeColor: Colors.orange,
                onChanged: (val) {
                  setState(() => settings.bhaktiModeEnabled = val);
                },
              )
            ],
          ),
          SizedBox(height: 8),
          if (settings.bhaktiModeEnabled) ...[
            Container(
              padding: EdgeInsets.all(12),
              decoration: BoxDecoration(color: Colors.orange.withOpacity(0.1), borderRadius: BorderRadius.circular(12), border: Border.all(color: Colors.orange.withOpacity(0.3))),
              child: Column(
                children: [
                  Text("Current Jap: ${settings.radhaJapCount} / ${settings.radhaJapTarget}", style: TextStyle(color: Colors.orange, fontWeight: FontWeight.bold)),
                  SizedBox(height: 8),
                  LinearProgressIndicator(value: settings.radhaJapCount / settings.radhaJapTarget, color: Colors.orange, backgroundColor: Colors.white12),
                ],
              ),
            ),
            SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
                    onPressed: () async {
                      if (_bridge.isConnected) {
                        final resp = await _bridge.sendRequest("process_radha_jap_flow", {"jap_count": settings.radhaJapCount, "ignore_count": _bhaktiIgnoreCount});
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
                        if (resp["flow_stage"] != "COMPLETED") {
                          setState(() => _bhaktiIgnoreCount++);
                        }
                      }
                    },
                    child: Text("Simulate Daily Reminder"),
                  ),
                ),
              ],
            )
          ] else
            Text("Detecting temple location or sacred attire via vision engine...", style: TextStyle(color: Colors.white38, fontSize: 11)),
        ],
      ),
    );
  }

  Widget _buildChildSafetySection(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.child_care, color: Colors.lightBlueAccent),
              SizedBox(width: 8),
              Text("Child Safety & Exam Engine", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
            ],
          ),
          SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Status: ${_isChildDetected ? 'CHILD DETECTED' : 'Adult Interaction'}", style: TextStyle(color: _isChildDetected ? Colors.orangeAccent : Colors.white70, fontSize: 12, fontWeight: FontWeight.bold)),
              Switch(
                value: _isChildDetected,
                onChanged: (val) async {
                  if (_bridge.isConnected) {
                    final resp = await _bridge.sendRequest("detect_child_interaction", {"vision_age_est": val ? 10 : 30, "voice_pitch_hz": val ? 250 : 120});
                    setState(() => _isChildDetected = resp["is_child"]);
                    if (_isChildDetected) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
                  }
                },
              )
            ],
          ),
          if (_isChildDetected) ...[
            Divider(color: Colors.white12),
            Text("Exam Protocol Active for Gopal", style: TextStyle(color: Colors.lightBlueAccent, fontSize: 12, fontWeight: FontWeight.bold)),
            SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: GlossyButton(
                    label: "Simulate Wandering",
                    onPressed: () async {
                      if (_bridge.isConnected) {
                        final resp = await _bridge.sendRequest("enforce_exam_protocol", {"child_name": "Gopal", "activity": "wandering"});
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
                      }
                    },
                    icon: Icons.warning,
                    gradientColors: [Colors.orange, Colors.red],
                  ),
                ),
                SizedBox(width: 8),
                Expanded(
                  child: GlossyButton(
                    label: "Ask Study Question",
                    onPressed: () async {
                      if (_bridge.isConnected) {
                        final resp = await _bridge.sendRequest("enforce_exam_protocol", {"child_name": "Gopal", "activity": "asking_subject"});
                        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["response"])));
                      }
                    },
                    icon: Icons.help,
                    gradientColors: [Colors.blue, Colors.cyan],
                  ),
                ),
              ],
            )
          ]
        ],
      ),
    );
  }

  Widget _buildSmartHomeSection(double width) {
    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text("Hyper-Advanced Smart Home", style: TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.bold)),
              Icon(Icons.home, color: Colors.cyanAccent),
            ],
          ),
          SizedBox(height: 12),
          Wrap(
            spacing: 8,
            children: [
              ActionChip(
                label: Text("Study Mode", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: () async {
                  if (_bridge.isConnected) {
                    final resp = await _bridge.sendRequest("iot_automation_control", {"scene_mode": "Study Mode"});
                    setState(() => _iotDevices = resp["devices"]);
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["message"])));
                  }
                },
              ),
              ActionChip(
                label: Text("Sleep Mode", style: TextStyle(color: Colors.white, fontSize: 11)),
                backgroundColor: Colors.white10,
                onPressed: () async {
                  if (_bridge.isConnected) {
                    final resp = await _bridge.sendRequest("iot_automation_control", {"scene_mode": "Sleep Mode"});
                    setState(() => _iotDevices = resp["devices"]);
                    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(resp["message"])));
                  }
                },
              ),
            ],
          ),
          SizedBox(height: 12),
          ListView.builder(
            shrinkWrap: true,
            physics: NeverScrollableScrollPhysics(),
            itemCount: _iotDevices.length,
            itemBuilder: (context, index) {
              final d = _iotDevices[index];
              return Padding(
                padding: const EdgeInsets.symmetric(vertical: 4.0),
                child: Row(
                  children: [
                    Icon(d["type"] == "AC" ? Icons.ac_unit : d["type"] == "Light" ? Icons.lightbulb : d["type"] == "Lock" ? Icons.lock : Icons.videocam, color: Colors.cyanAccent, size: 18),
                    SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(d["name"], style: TextStyle(color: Colors.white, fontSize: 13, fontWeight: FontWeight.w500)),
                          Text("${d['protocol']} Protocol", style: TextStyle(color: Colors.white38, fontSize: 10)),
                        ],
                      ),
                    ),
                    Text(d["state"], style: TextStyle(color: d["state"] == "OFF" || d["state"] == "LOCKED" ? Colors.redAccent : Colors.greenAccent, fontSize: 11, fontWeight: FontWeight.bold)),
                  ],
                ),
              );
            },
          ),
        ],
      ),
    );
  }
}
