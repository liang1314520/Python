"""
Example that shows animated circles. The animation is run from Python.
Doing that in JS would be more efficient, but we have not implemented timers
yet.
"""

import math
import time

from flexx import app, ui


class Circle(ui.Label):
    CSS = """
    .flx-circle {
        background: #f00;
        border-radius: 10px;
        width: 10px;
        height: 10px;
    }
    """

class App(ui.Widget):
    def init(self):
        self._circles = []
        
        with ui.PinboardLayout():
            for i in range(32):
                x = math.sin(i*0.2)*0.3 + 0.5
                y = math.cos(i*0.2)*0.3 + 0.5
                w = Circle(pos=(x,y))
                self._circles.append(w)
        
        self.tick()
        # todo: animate in JS!
    
    def tick(self):
        t = time.time()
        for i, circle in enumerate(self._circles):
            x = math.sin(i*0.2 + t)*0.3 + 0.5
            y = math.cos(i*0.2 + t)*0.3 + 0.5
            circle.pos((x, y))
        app.call_later(0.03, self.tick)

if __name__ == '__main__':
    m = app.launch(App)
    app.run()
