#!/bin/python3.6
import numpy as np
import cv2
import easygui
import threading

import sys

from form import form

cameraLocation = '/dev/video0'
#cameraLocation = 'rtsp://192.168.1.103/live1.sdp'
cap = cv2.VideoCapture(cameraLocation)

# Variables
text_info = 'Test of subtitle'

def text_box():
    global text_info
    while (True):
        test = easygui.enterbox(text_info, "Title", "Score 1 - 10")


t1 = form()
#t1 = threading.Thread(target=text_box, args=[])
t1.start()

while (True):
    # Capture frame-by-frame
    ret, frame = cap.read()

    # Our operations on the frame come here
    gray = frame  # cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    # Write text
    font = cv2.FONT_HERSHEY_COMPLEX

    cv2.putText(gray, t1.getText(),
                (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH) / 4), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT) - 30)), font, 1,
                (100, 200, 100), 2, cv2.LINE_AA)

    # Display the resulting frame
    cv2.imshow('frame', gray)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything done, release the capture
cap.release()
cv2.destroyAllWindows()
sys.exit(1)

