import 'dart:convert';
import 'dart:io';
import 'dart:async' show StreamController;
import 'package:flutter/foundation.dart';

class PythonProcessBridge extends ChangeNotifier {
  static final PythonProcessBridge _instance = PythonProcessBridge._internal();
  factory PythonProcessBridge() => _instance;

  PythonProcessBridge._internal();

  Socket? _socket;
  Process? _pythonProcess;
  bool _isConnected = false;

  bool get isConnected => _isConnected;

  Future<void> startCoreEngine() async {
    if (_isConnected) return;

    try {
      // Start the python server process
      debugPrint("Starting Python Core Engine...");
      _pythonProcess = await Process.start('python', ['python_core/main.py']);
      
      // Handle stderr and stdout for debugging
      _pythonProcess!.stdout.transform(utf8.decoder).listen((data) {
        debugPrint("[Python Out]: $data");
      });
      _pythonProcess!.stderr.transform(utf8.decoder).listen((data) {
        debugPrint("[Python Err]: $data");
      });

      // Give the server a moment to bind and start listening
      await Future.delayed(Duration(seconds: 1));

      // Connect socket
      await connectSocket();
    } catch (e) {
      debugPrint("Failed to start Python Core Engine: $e");
    }
  }

  Future<void> connectSocket({String host = '127.0.0.1', int port = 8765}) async {
    try {
      _socket = await Socket.connect(host, port);
      _isConnected = true;
      notifyListeners();
      debugPrint("Successfully connected to Python Core Engine socket on $host:$port");
    } catch (e) {
      _isConnected = false;
      notifyListeners();
      debugPrint("Socket connection failed: $e");
    }
  }

  Future<Map<String, dynamic>> sendRequest(String action, [Map<String, dynamic>? payload]) async {
    if (!_isConnected || _socket == null) {
      return {"status": "error", "message": "Not connected to Core Engine"};
    }

    try {
      final request = {
        "action": action,
        "payload": payload ?? {}
      };
      _socket!.write(jsonEncode(request) + "\n");
      await _socket!.flush();

      // Simple response reading await (note: a full production bridge would multiplex/queue queries)
      final completer = Completer<Map<String, dynamic>>();
      final subscription = _socket!.transform(utf8.decoder).listen((data) {
        try {
          final response = jsonDecode(data.trim());
          if (response is Map<String, dynamic>) {
            completer.complete(response);
          } else {
            completer.complete({"status": "error", "message": "Invalid response type"});
          }
        } catch (e) {
          completer.complete({"status": "error", "message": "JSON Parse failed: $e"});
        }
      });

      final result = await completer.future.timeout(Duration(seconds: 15), onTimeout: () {
        return {"status": "error", "message": "Query timed out"};
      });

      await subscription.cancel();
      return result;
    } catch (e) {
      return {"status": "error", "message": "Bridge request failed: $e"};
    }
  }

  void disposeBridge() {
    _socket?.destroy();
    _pythonProcess?.kill();
    _isConnected = false;
    notifyListeners();
  }
}

class Completer<T> {
  final _futureCompleter = FutureCompleter<T>();
  Future<T> get future => _futureCompleter.future;
  void complete(T val) => _futureCompleter.complete(val);
}

class FutureCompleter<T> {
  late final Future<T> future;
  late final Function(T) complete;

  FutureCompleter() {
    final completer = StreamController<T>.broadcast();
    future = completer.stream.first;
    complete = (val) {
      completer.add(val);
      completer.close();
    };
  }
}
