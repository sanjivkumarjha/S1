import os
import sys
import requests

def download_file_from_google_drive(file_id, destination):
    print(f"Downloading file ID {file_id} from Google Drive...")
    URL = "https://docs.google.com/uc?export=download"

    session = requests.Session()
    response = session.get(URL, params={'id': file_id, 'confirm': 't'}, stream=True)
    token = get_confirm_token(response)

    if token:
        params = {'id': file_id, 'confirm': token}
        response = session.get(URL, params=params, stream=True)

    save_response_content(response, destination)

def get_confirm_token(response):
    for key, value in response.cookies.items():
        if key.startswith('download_warning'):
            return value
    return None

def save_response_content(response, destination):
    CHUNK_SIZE = 32768
    with open(destination, 'wb') as f:
        for chunk in response.iter_content(CHUNK_SIZE):
            if chunk:  # filter out keep-alive new chunks
                f.write(chunk)

def main():
    file_id = "1PmlUdJ8o3Fkk6Vy5zuEKPcltiBKDbBVz"
    os.makedirs("flutter_snaper/assets/images", exist_ok=True)
    temp_destination = "flutter_snaper/assets/images/temp_app_logo"
    final_destination = "flutter_snaper/assets/images/app_logo.png"

    try:
        download_file_from_google_drive(file_id, temp_destination)
        print("Download complete. Validating file...")

        # Read first few bytes to check if it's an HTML page (Google block/error) or image
        with open(temp_destination, 'rb') as f:
            header = f.read(100)
            if b"<!DOCTYPE html>" in header or b"<html" in header or b"google.com" in header:
                print("Error: The downloaded content appears to be an HTML page, not an image.", file=sys.stderr)
                print("Google Drive URL may be private, restricted, or requires login.", file=sys.stderr)
                sys.exit(1)

        # Check if PIL is available to convert/verify image
        try:
            from PIL import Image
            img = Image.open(temp_destination)
            img.save(final_destination, "PNG")
            os.remove(temp_destination)
            print(f"Success: Image verified, converted and saved to {final_destination}")
        except ImportError:
            # If PIL is not available, we can try to rename if it's already a valid PNG/JPEG
            # (or we can install Pillow, but let's do a basic rename as fallback)
            import shutil
            shutil.copyfile(temp_destination, final_destination)
            os.remove(temp_destination)
            print(f"Saved logo directly to {final_destination} (PIL not installed, validation basic).")
    except Exception as e:
        print(f"Exception occurred during download or validation: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
