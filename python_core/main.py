import sys
import os
import json
import socket
import threading
import traceback

# Add current directory to path to allow absolute imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from core.engine import AssistantEngine

def handle_client(client_socket, engine):
    print("New connection established.")
    buffer = ""
    try:
        while True:
            data = client_socket.recv(4096)
            if not data:
                break
            buffer += data.decode('utf-8')
            while "\n" in buffer:
                line, buffer = buffer.split("\n", 1)
                line = line.strip()
                if not line:
                    continue
                try:
                    request = json.loads(line)
                    response = engine.dispatch(request)
                    client_socket.sendall((json.dumps(response) + "\n").encode('utf-8'))
                except Exception as e:
                    error_resp = {
                        "status": "error",
                        "error": str(e),
                        "trace": traceback.format_exc()
                    }
                    client_socket.sendall((json.dumps(error_resp) + "\n").encode('utf-8'))
    except Exception as e:
        print(f"Connection error: {e}")
    finally:
        client_socket.close()
        print("Connection closed.")

def start_server(port=8765):
    engine = AssistantEngine()
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    try:
        server.bind(('127.0.0.1', port))
        server.listen(5)
        print(f"Snaper AI Assistant Core Engine running on port {port}...")
    except Exception as e:
        print(f"Failed to bind socket server: {e}", file=sys.stderr)
        sys.exit(1)

    while True:
        try:
            client_sock, _ = server.accept()
            thread = threading.Thread(target=handle_client, args=(client_sock, engine))
            thread.daemon = True
            thread.start()
        except KeyboardInterrupt:
            print("Shutting down core engine server.")
            break
        except Exception as e:
            print(f"Error accepting connection: {e}", file=sys.stderr)

if __name__ == "__main__":
    port = int(os.environ.get("SNAPER_CORE_PORT", 8765))
    start_server(port)
