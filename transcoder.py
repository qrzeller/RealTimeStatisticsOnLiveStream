import cv2


#inspired by http://www.chioka.in/python-live-video-streaming-example/

class transcoder(object):

    frame = None
    def jpeg(self):
        mjpg = cv2.imencode('.jpg', frame)
        return mjpg.tobytes()

    def setframe(self, img):
        global frame
        frame = img
