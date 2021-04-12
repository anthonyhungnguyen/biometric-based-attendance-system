#!/usr/bin/env python
#
# Copyright 2016 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Example Kafka Producer.
# Reads lines from stdin and sends to Kafka.
#

# from confluent_kafka import Producer
# import sys

# if __name__ == '__main__':
#     if len(sys.argv) != 3:
#         sys.stderr.write(
#             'Usage: %s <bootstrap-brokers> <topic>\n' % sys.argv[0])
#         sys.exit(1)

#     broker = sys.argv[1]
#     topic = sys.argv[2]

#     # Producer configuration
#     # See https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md
#     conf = {'bootstrap.servers': broker}

#     # Create Producer instance
#     p = Producer(**conf)

#     # Optional per-message delivery callback (triggered by poll() or flush())
#     # when a message has been successfully delivered or permanently
#     # failed delivery (after retries).
#     def delivery_callback(err, msg):
#         if err:
#             sys.stderr.write('%% Message failed delivery: %s\n' % err)
#         else:
#             sys.stderr.write('%% Message delivered to %s [%d] @ %d\n' %
#                              (msg.topic(), msg.partition(), msg.offset()))

#     # Read lines from stdin, produce each line to Kafka
#     for line in sys.stdin:
#         try:
#             # Produce line (without newline)
#             p.produce(topic, line.rstrip(), callback=delivery_callback)

#         except BufferError:
#             sys.stderr.write('%% Local producer queue is full (%d messages awaiting delivery): try again\n' %
#                              len(p))

#         # Serve delivery callback queue.
#         # NOTE: Since produce() is an asynchronous API this poll() call
#         #       will most likely not serve the delivery callback for the
#         #       last produce()d message.
#         p.poll(0)

#     # Wait until all messages have been delivered
#     sys.stderr.write('%% Waiting for %d deliveries\n' % len(p))
#     p.flush()


from confluent_kafka import Producer
import os
import sys
import cv2
import base64
import json
from datetime import datetime
from __init__ import PYTHON_PATH


CHECKED_PATH = os.path.join(PYTHON_PATH, "ailibs_data/log/image/phuc.jpg")


def increase_brightness(img, value):
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    h, s, v = cv2.split(hsv)

    lim = 255 - value
    v[v > lim] = 255
    v[v <= lim] += value

    final_hsv = cv2.merge((h, s, v))
    img = cv2.cvtColor(final_hsv, cv2.COLOR_HSV2BGR)
    return img

def augment(image):
    image_list = [image]
    image_list.append(increase_brightness(image, 30))
    image_flip = cv2.flip(image,1)
    image_list.append(image_flip)
    image_list.append(increase_brightness(image_flip, 30))
    image_list.append(increase_brightness(image_flip, 50))
    return image_list


if __name__ == '__main__':
    if len(sys.argv) != 3:
        sys.stderr.write(
            'Usage: %s <bootstrap-brokers> <topic>\n' % sys.argv[0])
        sys.exit(1)

    broker = sys.argv[1]
    topic = sys.argv[2]

    # Producer configuration
    # See https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md
    conf = {'bootstrap.servers': broker}

    # Create Producer instance
    p = Producer(**conf)

    # Optional per-message delivery callback (triggered by poll() or flush())
    # when a message has been successfully delivered or permanently
    # failed delivery (after retries).
    def delivery_callback(err, msg):
        if err:
            sys.stderr.write('%% Message failed delivery: %s\n' % err)
        else:
            sys.stderr.write('%% Message delivered to %s [%d] @ %d\n' %
                             (msg.topic(), msg.partition(), msg.offset()))

    # Read lines from stdin, produce each line to Kafka
    # with open("image/cat.jpeg", "rb") as image_file:
    #     encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    # print(encoded_string)
    # log = {'userID': '1752259', 'semester': '201', 'groupCode': 'CC01', 'subjectID': 'CO0000', 'timestamp': str(datetime.now()),
    #        'deviceID': '1', 'imgSrcBase64': encoded_string, 'teacherID': '201'}
    # log = {
    #     "semester": "201",
    #     "groupCode": "CC01",
    #     "subjectID": "CO3068",
    #     "teacherID": "2",
    #     "startTime": "2021-02-28T15:54:55.967",
    #     "endTime": "2021-02-28T15:54:55.967",
    #     "deviceID": "1"
    # }
    # with open(CHECKED_PATH, "rb") as image_file:
    #     image = image_file.read()
    #     print(image)
    image = cv2.imread(CHECKED_PATH)
    scale_percent = 10 # percent of original size
    width = int(image.shape[1] * scale_percent / 100)
    height = int(image.shape[0] * scale_percent / 100)
    dim = (width, height)
    print(dim)
    # resize image
    image = cv2.resize(image, dim, interpolation = cv2.INTER_AREA)
    image_list = augment(image)  
    for id, img in enumerate(image_list):
        cv2.imwrite(f'test{id}.jpg', img)
        retval, buffer = cv2.imencode('.jpg', img)
        encoded_string = base64.b64encode(buffer).decode('utf-8')
        log = {
            "userId": "1752041",
            "photo": encoded_string
        }
        msg = json.dumps(log)
        try:
            # Produce line (without newline)
            p.produce(topic, msg, callback=delivery_callback)

        except BufferError:
            sys.stderr.write('%% Local producer queue is full (%d messages awaiting delivery): try again\n' %
                            len(p))

    # Serve delivery callback queue.
    # NOTE: Since produce() is an asynchronous API this poll() call
    #       will most likely not serve the delivery callback for the
    #       last produce()d message.
    p.poll(0)

    # Wait until all messages have been delivered
    sys.stderr.write('%% Waiting for %d deliveries\n' % len(p))
    p.flush()

