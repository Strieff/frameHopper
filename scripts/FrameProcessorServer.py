import socket
import cv2
import os
from pathlib import Path
import win32api as w32

video_path = ""
cache_path = ""
frame_cache = []

# TODO: overloading for loading batches

def start_server():
    HOST = '127.0.0.1'
    PORT = 65432

    with socket.socket(socket.AF_INET,socket.SOCK_STREAM) as s:
        s.bind((HOST,PORT))
        s.listen()

        print(f'Server started, listening on port: {PORT}')

        conn,addr = s.accept()
        with conn:
            print(f'Conneted by: {addr}')
            
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                
                request = data.decode('utf-8')
                print(f'Request: {request}')
                response = handle_request(request)
                print(f'Response: {response}')
                conn.sendall(response.encode('utf-8'))


def handle_request(request: str):
    global video_path,cache_path

    command,argument,path = request.split(';')
    path = path.replace('\r', '').replace('\n', '')

    if command == '-1': # close program
        print('Shutting down...')
        exit()
    elif command == '0': #set cache pat
        if path.isascii():
            cache_path = str(os.path.abspath(path))
        else:
            cache_path = w32.GetShortPathName(str(os.path.abspath(path)))    

        print(f'Cache set to: {cache_path}')

        return f'{cache_path}\n'
    elif command == '1': # load first set
        if path.isascii():
            path = str(os.path.abspath(path))
        else:
            path = w32.GetShortPathName(str(os.path.abspath(path)))

        video_path = path    

        print(f'Video path: {video_path}')  

        load_first_batch()

        return f'{video_path}\n'
    elif command == '2': # get video properties
        print(f'Loading video properties of: {video_path}')

        return f'{get_video_properties(video_path)}\n'
    elif command == '3': # load given set
        print('Loading batch')
        load_batch(argument)

        return 'OK\n'
    elif command == '4': # get properties of path
        print(f'Loading video properties of: {path}')

        return f'{get_video_properties(path)}\n'

    


def load_first_batch():
    global video_path,cache_path,frame_cache

    # Create a VideoCapture object
    cap = cv2.VideoCapture(video_path)

    # Check if the video opened successfully
    if not cap.isOpened():
        raise ValueError(f'Could not open the video file: {video_path}')
    
    # Clear frame cache and read all frames and store them in the list
    # TODO: set limit on frame amount - first come first gone
    frame_cache = []
    while True:
        ret,frame = cap.read()
        if not ret:
            break
        frame_cache.append(frame)
    
    # Release the VideoCapture object
    cap.release()

    # Create a directory to save frames to
    file_name = Path(video_path).name
    file_path = os.path.join(cache_path,file_name)
    os.makedirs(file_path, exist_ok=True)

    for i in range(min(100,len(frame_cache))):
        frame_path = os.path.join(file_path,f'{i}.jpg')
        cv2.imwrite(frame_path,frame_cache[i])

    print(f'First hundred frames saved to: {file_path}')

def load_batch(set):
    global frame_cache,cache_path,video_path

    if not frame_cache:
        raise ValueError('No frames were loaded')
    
    # Determine the starting index for the batch
    start_index = int(set)*100

    # Check if the starting index exceeds the maximum frame index
    if start_index >= len(frame_cache):
        print("Error: Starting index exceeds maximum frame index.")
        return
    
    file_name = Path(video_path).name
    file_path = os.path.join(cache_path,file_name)

    end_index = min(int(start_index) + 100,int(len(frame_cache)))

    for i in range(start_index,end_index):
        frame_path = os.path.join(file_path,f'{i}.jpg')
        cv2.imwrite(frame_path,frame_cache[i])

    print(f'Batch from {start_index} to {end_index} saved to: {file_path}')    


def get_video_properties():
    global video_path

    # Open the video file
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        raise ValueError(f'Could not open the video file: {video_path}')
    
    # Get the properties
    length_in_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    image_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    image_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    frame_rate = cap.get(cv2.CAP_PROP_FPS)
    duration_in_milliseconds = (length_in_frames / frame_rate) * 1000

    # Release the video capture object
    cap.release()

    # Construct the result string
    result = f"{length_in_frames};{image_height};{image_width};{frame_rate:.2f};{int(duration_in_milliseconds)}"

    return result

def get_video_properties(path):
    # Open the video file
    cap = cv2.VideoCapture(path)

    if not cap.isOpened():
        raise ValueError(f'Could not open the video file: {path}')

    # Get the properties
    length_in_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    image_height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    image_width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    frame_rate = cap.get(cv2.CAP_PROP_FPS)
    duration_in_milliseconds = (length_in_frames / frame_rate) * 1000

    # Release the video capture object
    cap.release()

    # Construct the result string
    result = f"{length_in_frames};{image_height};{image_width};{frame_rate:.2f};{int(duration_in_milliseconds)}"

    return result


if __name__ == "__main__":
    start_server()