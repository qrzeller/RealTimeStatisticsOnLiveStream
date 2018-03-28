#!/usr/bin/env python
#
# Project: Video Streaming with Flask
# Modified to support streaming out with webcams, and not just raw JPEGs.
# Credits to Miguel Grinberg : http://blog.miguelgrinberg.com/post/video-streaming-with-flask
#
# Usage:
# 1. Install Python dependencies: cv2, flask. (wish that pip install works like a charm)
# 2. Run "python main.py".
# 3. Navigate the browser to the local webpage.
from flask import Flask, render_template, Response, request
from camera import VideoCamera

app = Flask(__name__)
cam = None
@app.route('/')
def index():
    return render_template('index.html')

@app.route('/<path:path>')
def send_js(path):
    return render_template(path)

def gen(camera):
    while True:
        frame = camera.get_frame()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')

@app.route('/video_feed')
def video_feed():
    global cam
    if cam is None:
        cam = VideoCamera()
    return Response(gen(cam),
                    mimetype='multipart/x-mixed-replace; boundary=frame')



@app.route('/send/',methods=['POST','GET'])
def form():
    print("Updating text")
    print(request.args)
    if request.args.get('subtitle', None):
       input_text = request.args['subtitle']
       cam.update_text(input_text,request.args['col'])
    return render_template('formpage.html')

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True,threaded=True)