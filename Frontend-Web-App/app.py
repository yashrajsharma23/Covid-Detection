import base64
import numpy as np
import io
from PIL import Image
from pathlib import Path

import re

# pip install tensorflow == 2.2.0
import tensorflow as tf

from tensorflow import keras
# from keras.models import Sequential, load_model
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import ImageDataGenerator, img_to_array
from flask import request, render_template
from flask import jsonify
from flask import Flask

app = Flask(__name__, template_folder='./templates', static_folder='./static')


# https://deeplizard.com/learn/video/XgzxH6G-ufA

def get_model():
    global model
    model = load_model('model/Custom_11.h5')  # 'VGG16_cats_and_dogs.h5')
    print(" * Model loaded!")

def preprocess_image(image, target_size):
    if image.mode != "RGB":
        image = image.convert("RGB")

    image = image.resize(target_size)
    print('Image Size:',image.size)

    image = img_to_array(image)
    image = np.expand_dims(image, axis=0)
    return image


print(" * Loading Keras model...")
get_model()


@app.route("/predict", methods=['GET', 'POST'])  # ,"GET"])
def predict():
    print('In: 1')

    message = request.get_json(force=True)  # json#
    encoded = message['image']
    image_data = re.sub('^data:image/.+;base64,', '', encoded)

    # print(img_type)
    decoded = base64.b64decode(image_data)#encoded)
    print('Image Type::: ',type(decoded))
    image = Image.open(io.BytesIO(decoded))
    processed_image = preprocess_image(image, target_size=(256, 256))

    prediction = model.predict(processed_image).tolist()

    response = {
        'prediction': {
            'Negative': prediction[0][0],
            'Positive': prediction[0][1]
        }
    }
    print(response)
    return jsonify(response)


@app.route('/index')
@app.route('/')
def index():
    return render_template('predict.html', title='Home')


@app.route("/sample")  # , methods=["POST","GET"])
def running():
    return 'Flask is running!!!!'


# flask run --host=0.0.0.0

if __name__ == 'app':
    print(__name__)
    app.run(debug=True, host='0.0.0.0', port=5000)
