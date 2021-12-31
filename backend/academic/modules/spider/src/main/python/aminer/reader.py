import json


class Reader:
    def __init__(self, src, filters=None):
        if filters is None:
            filters = []
        self.src = src
        self.filters = filters
        self.count = 0

    def next_object(self):
        line = self.src.readline()
        self.count += 1
        while line:
            obj = json.loads(line)
            passed = True
            for flt in self.filters:
                if not flt(obj):
                    passed = False
                    break
            if passed:
                return obj
            line = self.src.readline()
            self.count += 1
        return None
