import socket
#import FrameProcessor as fp
import cv2
import os
from pathlib import Path
import shutil

video_path = ""
all_frames = []
cache_path = ""

#TODO return and send response other than OK

def handle_request(request):
    command,argument,path = request.split(';')

    if command == '0':
        global video_path
        video_path = path

        global all_frames
        all_frames = []

        print(video_path)
        save_first_hundred_frames()
        return "OK\n"
    elif command == '1':
        load_batch(argument)
        return "OK\n"
    elif command == '200':
        global cache_path
        cache_path = path[:-2]

        return "OK\n"
        #check if logging is enabled
    elif command == '-1':
        print('Shutting down...')
        exit()


def start_server():
    HOST = '127.0.0.1'
    PORT = 65432

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen()

        print("Started, listening on port: ", PORT)
        conn, addr = s.accept()
        with conn:
            print('Connected by', addr)
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                request = data.decode('utf-8')
                print("Received:", request)
                response = handle_request(request)
                print("Prepared response: ", response)
                conn.sendall(response.encode('utf-8'))

def save_first_hundred_frames():
    global all_frames

    # Create a VideoCapture object
    cap = cv2.VideoCapture(video_path)

    # Check if the video opened successfully
    if not cap.isOpened():
        print("Error: Unable to open video.")
        return
    
    # Read all frames and store them in the list
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        all_frames.append(frame)

    # Release the VideoCapture object
    cap.release()    

    # Create a directory to save frames
    file_name = Path(video_path).name[:-2]
    file_path = os.path.join(cache_path,file_name)
    os.makedirs(file_path, exist_ok=True)

    # Save the first hundred frames as JPEG images
    for i in range(min(100, len(all_frames))):
        frame_path = os.path.join(file_path, f"{i}.jpg")
        cv2.imwrite(frame_path, all_frames[i])


    print(f"First hundred frames saved in directory: {file_path}")


def load_batch(hundred):
    global all_frames

    # Check if frames have been loaded
    if not all_frames:
        print("Error: No frames have been loaded.")
        return
    
    # Determine the starting index for the batch
    start_index = int(hundred) * 100

    # Check if the starting index exceeds the maximum frame index
    if start_index >= len(all_frames):
        print("Error: Starting index exceeds maximum frame index.")
        return

    # Create a directory to save frames
    file_name = Path(video_path).name[:-2]
    file_path = os.path.join(cache_path,file_name)
    os.makedirs(file_path, exist_ok=True)

    # Calculate the end index for the batch
    end_index = min((int(start_index) + 100), int(len(all_frames)))

    # Save the batch of frames as JPEG images
    for i in range(int(start_index), int(end_index)):
        frame_path = os.path.join(file_path, f"{i}.jpg")
        cv2.imwrite(frame_path, all_frames[i])

    print(f"Batch of frames starting from frame {start_index} saved in directory: {file_path}")



# def open_file(path):
if __name__ == "__main__":
    start_server()
