import cv2
import imgkit as imgkit


class VideoCamera(object):
    def __init__(self):
        self.subtitle = "Some text"
        self.col = (100, 200, 100)

        # Using OpenCV to capture from device 0. If you have trouble capturing
        # from a webcam, comment the line below out and use a video file
        # instead.
        # self.video = cv2.VideoCapture(0)
        self.video = cv2.VideoCapture("http://172.20.1.52:80/live")
        # If you decide to use video.mp4, you must have this file in the folder
        # as the main.py.
        # self.video = cv2.VideoCapture('video.mp4')

    def __del__(self):
        self.video.release()

    def get_frame(self):
        success, image = self.video.read()
        # We are using Motion JPEG, but OpenCV defaults to capture raw images,
        # so we must encode it into JPEG in order to correctly display the
        # video stream.
        # Write text
        font = cv2.FONT_HERSHEY_COMPLEX

        cv2.putText(image, self.subtitle,
                    (int(self.video.get(cv2.CAP_PROP_FRAME_WIDTH) / 8),
                     int(self.video.get(cv2.CAP_PROP_FRAME_HEIGHT) - 30)), font, 0.8,
                    self.col, 2, cv2.LINE_AA)
        ret, jpeg = cv2.imencode('.jpg', image)

        return jpeg.tobytes()

    def update_text(self, txt, col):
        self.subtitle = txt
        if col == "yes":
            self.col = (100, 200, 100)
        else:
            self.col = (0,0,0)
