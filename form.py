import threading

import sys

import easygui

text = "default"


class form(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)

    def run(self):
        self.text_box()

    def text_box(self):
        global text
        score = "Score"
        while (True):
            text = easygui.enterbox(score, "Title", "Score 1 - 10")

    def getText(self):
        return text
