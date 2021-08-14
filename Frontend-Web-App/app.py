import base64
import numpy as np
import io
from PIL import Image
import re

from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from flask import request, render_template
from flask import jsonify
from flask import Flask

app = Flask(__name__, template_folder='./templates', static_folder='./static')

# PROJECT_NAME = 'covid_detection'
# CREDENTIALS = 'static/covid-google-service.json'
# MODEL_PATH = 'gs://covid_detection/Custom_11.h5'

def get_model():
    global model
    try:
        model = load_model('model/Custom_11.h5')  # 'VGG16_cats_and_dogs.h5')
        response = {
            'response': {
                'success': 'Model Loaded Successfully.'
            }
        }
        print(" * Model loaded!")

        print(response)
        return jsonify(response)
    except Exception as e:
        response = {
            'response': {
                'error': 'Model Loading failed.<br>Something went wrong, please try again later..'
            }
        }
        print(response)
        return jsonify(response)

def preprocess_image(image, target_size):
    if image.mode != "RGB":
        image = image.convert("RGB")

    image = image.resize(target_size)
    print('Image Size:',image.size)

    image = img_to_array(image)
    image = np.expand_dims(image, axis=0)
    return image

# get_model()

@app.route("/loadModel", methods=['GET','POST'])
def loadModel():
    print(" * Loading Keras model...")
    return get_model()

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

    respone = ''
    isNegative = True

    prediction = model.predict(processed_image).tolist()
    negative = prediction[0][0] * 100
    positive = prediction[0][1] * 100
    print('Negative',negative,':', 'Positive:',positive)
    if float(prediction[0][0])>float(prediction[0][1]):
        #respone = 'Patient X-ray report seems {}% Covid Negative'.format(negative)
        respone = 'Patient X-ray report seems Covid Negative'
        isNegative=True
    else:
        #respone = 'Patient X-ray report seems {}% Covid Positive'.format(positive)
        respone = 'Patient X-ray report seems Covid Positive'
        isNegative = False

    response = {
        'prediction': {
            'response': respone,
            'isNegative': isNegative
            # 'Negative': prediction[0][0],
            # 'Positive': prediction[0][1]
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
    #app.run(debug=True, host='0.0.0.0', port=5000)
    app.run()
