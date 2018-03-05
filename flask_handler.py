import transcoder
from flask import Flask, render_template, Response

class mjpegstream(object):
    app = Flask(__name__)

    cap = None
    def init(img):
        cap = img

    @app.route('/')
    def index(self):
        return render_template('index.html')

    def gen(camera):
        while True:
            frame = camera.get_frame()
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')


    @app.route('/video_feed')
    def video_feed(self):
        return Response(transcoder.jpeg(),
                        mimetype='multipart/x-mixed-replace; boundary=frame')


    app.run(host='0.0.0.0', debug=True)