import threading

import sys

import easygui

text = "default"


class form(threading.Thread):

    def __init__(self):
        threading.Thread.__init__(self)


    def run(self):
        try:
            self.text_box()
        except KeyboardInterrupt:
            pass



    def text_box(self):
        global text
        score = "Score"
        while (True):
            text = easygui.enterbox(score, "Title", "Score 1 - 10")

    def getText(self):
        return text
