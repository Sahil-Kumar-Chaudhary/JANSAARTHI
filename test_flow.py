import subprocess
import xml.etree.ElementTree as ET
import time
import sys

ADB_PATH = r"C:\Users\sahil\AppData\Local\Android\Sdk\platform-tools\adb.exe"
PACKAGE = "com.example.jansaarthi"

def adb(args):
    cmd = [ADB_PATH] + args
    result = subprocess.run(cmd, capture_output=True, text=True)
    return result.stdout.strip()

def dump_ui():
    adb(["shell", "uiautomator", "dump", "/data/local/tmp/uidump.xml"])
    xml_data = adb(["shell", "cat", "/data/local/tmp/uidump.xml"])
    return xml_data

def find_element(xml_data, text=None, content_desc=None, class_name=None):
    if not xml_data: return None
    try:
        root = ET.fromstring(xml_data)
        for node in root.iter('node'):
            if text and text.lower() not in node.attrib.get('text', '').lower(): continue
            if content_desc and content_desc.lower() not in node.attrib.get('content-desc', '').lower(): continue
            if class_name and class_name != node.attrib.get('class'): continue
            
            bounds = node.attrib.get('bounds') # [x1,y1][x2,y2]
            if bounds:
                bounds = bounds.replace('][', ',').replace('[', '').replace(']', '').split(',')
                x1, y1, x2, y2 = map(int, bounds)
                cx = (x1 + x2) // 2
                cy = (y1 + y2) // 2
                return (cx, cy)
    except Exception as e:
        print(f"Error parsing XML: {e}")
    return None

def click(x, y):
    adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1)

def input_text(text):
    # Escape spaces for adb
    text = text.replace(" ", "%s")
    adb(["shell", "input", "text", text])
    time.sleep(0.5)
    
def click_text(text, exact=False):
    for i in range(3): # wait for element
        xml = dump_ui()
        coord = find_element(xml, text=text)
        if coord:
            click(coord[0], coord[1])
            return True
        time.sleep(1)
    print(f"Element with text '{text}' not found")
    return False

def click_desc(desc):
    for i in range(3):
        xml = dump_ui()
        coord = find_element(xml, content_desc=desc)
        if coord:
            click(coord[0], coord[1])
            return True
        time.sleep(1)
    print(f"Element with desc '{desc}' not found")
    return False

print("Restarting app...")
adb(["shell", "pm", "clear", PACKAGE])
adb(["shell", "am", "start", "-n", f"{PACKAGE}/.MainActivity"])
time.sleep(3)

print("Testing Registration...")
click_text("Get Started") # or maybe language selection first?
time.sleep(1)
click_text("English")
click_text("Continue")
time.sleep(1)

# we might be at Login or Get Started
xml = dump_ui()
if find_element(xml, text="Create Account"):
    click_text("Create Account")

# Type details
print("Typing registration details...")
# Focus Name field
xml = dump_ui()
click(100, 300) # approximate guess, need to be smarter, let's dump
print(xml)
