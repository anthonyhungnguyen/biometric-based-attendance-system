import os
import sys
import time
from datetime import datetime

PYTHON_PATH = os.path.abspath(os.path.join(
    os.path.abspath(__file__), "..", ".."))
os.chdir(PYTHON_PATH)
sys.path.insert(0, PYTHON_PATH)
