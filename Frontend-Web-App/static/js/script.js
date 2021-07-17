//selecting all required elements
const dropArea = document.querySelector(".drag-area"),
    dragText = dropArea.querySelector("header"),
    button = dropArea.querySelector("button"),
    input = dropArea.querySelector("input");

var btn_area = document.getElementById("btn-area");

var predict_btn = document.getElementById('predict-button');
var reset_btn = document.getElementById('reset-button');

var pred_tag = document.getElementById('pred-tag');

var negative = document.getElementById('Covid-Negative');
var positive = document.getElementById('Covid-Positive');
var error = document.getElementById('error-disp');

var error_tag = document.getElementById('error-tag');
var neg_tag = document.getElementById('neg-tag');
var pos_tag = document.getElementById('pos-tag');

let file; //this is a global variable and we'll use it inside multiple functions
button.onclick = () => {
    input.click(); //if user click on the button then the input also clicked
}
input.addEventListener("change", function () {
    //getting user select file and [0] this means if user select multiple files then we'll select only the first one
    file = this.files[0];
    dropArea.classList.add("active");
    showFile(); //calling function
});
//If user Drag File Over DropArea
dropArea.addEventListener("dragover", (event) => {
    event.preventDefault(); //preventing from default behaviour
    dropArea.classList.add("active");
    dragText.textContent = "Release to Upload File";
});
//If user leave dragged File from DropArea
dropArea.addEventListener("dragleave", () => {
    dropArea.classList.remove("active");
    dragText.textContent = "Drag & Drop to Upload File";
});
//If user drop File on DropArea
dropArea.addEventListener("drop", (event) => {
    event.preventDefault(); //preventing from default behaviour
    //getting user select file and [0] this means if user select multiple files then we'll select only the first one
    file = event.dataTransfer.files[0];
    showFile(); //calling function
});

let fileReader = new FileReader(); //creating new FileReader object

let imgTag;

function showFile() {
    btn_area.style.display = 'block';
    predict_btn.style.display='block';
    reset_btn.style.display='none';

    error_fun(null);
    let fileType = file.type; //getting selected file type
    let validExtensions = ["image/jpeg", "image/JPEG", "image/JPG", "image/jpg", "image/png"]; //adding some valid image extensions in array
    console.log(fileType)
    if (validExtensions.includes(fileType)) { //if user selected file is an image file

        fileReader.onload = () => {
            let fileURL = fileReader.result; //passing user file source in fileURL variable
            // UNCOMMENT THIS BELOW LINE. I GOT AN ERROR WHILE UPLOADING THIS POST SO I COMMENTED IT
            imgTag = `<img src="${fileURL}" alt="image" id="img_tag">`; //creating an img tag and passing user selected file source inside src attribute
            dropArea.innerHTML = imgTag; //adding that created img tag inside dropArea container

            if (fileURL.includes("jpg")) {
                base64Image = fileURL.replace("data:image/jpg;base64,", "");
            } else if (fileURL.includes("JPG")) {
                base64Image = fileURL.replace("data:image/JPG;base64,", "");
            } else if (fileURL.includes("jpeg")) {
                base64Image = fileURL.replace("data:image/jpeg;base64,", "");
            } else if (fileURL.includes("png")) {
                base64Image = fileURL.replace("data:image/png;base64,", "");
            } else {
                base64Image = fileURL;//.replace("data:image/png;base64,", "");
            }
        }
        fileReader.readAsDataURL(file);
    } else {
        alert("This is not an Image File!");
        dropArea.classList.remove("active");
        dragText.textContent = "Drag & Drop to Upload File";
    }
}

let base64Image;

function reset() {
    btn_area.style.display = 'none';

    predict_btn.style.display='block';
    reset_btn.style.display='none';

    base64Image = null
    let imgTag = `<img src="" alt="image" hidden style="visibility: hidden">`;
    let tag = '<div class="drag-area" id="drag-tag" style="margin:0 auto; margin-bottom: 10px">\n' +
        '    <div class="icon"><i class="fas fa-cloud-upload-alt"></i></div>\n' +
        '    <header>Drag & Drop to Upload File</header>\n' +
        '    <span>OR</span>\n' +
        '    <button>Browse File</button>\n' +
        '    <input type="file" id="myfile" hidden>\n' +
        '</div>\n';
    dropArea.innerHTML = tag;//imgTag;

    dropArea.classList.add("active");
    dragText.textContent = "Release to Upload File";

    error_fun(null)
}

const loader = document.querySelector('#loading');
function displayLoading(){
    loader.classList.add('display');
    setTimeout(()=>{
        loader.classList.remove('display');
    }, 5000);
}

function hideLoading(){
    loader.classList.remove('display');
}

function predict() {
    predict_btn.style.display='none';
    reset_btn.style.display='block';

    error.style.display = "none";

    console.log('Calling Predict button')
    if (base64Image == null) {
        error_fun('Please Upload Image.')
        return;
    }
    displayLoading();

    let message = {
        image: base64Image
        //image_type: fileType
    }
    // console.log(message);
    const url = "http://127.0.0.1:5000/predict";

    const other_params = {
        headers: {"content-type": "application/json;charset=UTF-8"},
        body: JSON.stringify(message),
        method: "POST"
    };

    fetch(url, other_params).then(function (response) {
        if (response.ok) {
            response.json().then(json => {
                console.log(json)
                success(json)
            });
        } else {
            error_fun("Could not reach the API: " + response.statusText+"." +
                "<br>Please try again later or Retry with different image");
            // throw new Error("Could not reach the API: " + response.statusText);
        }
        hideLoading();
    }).then(function (data) {
    }).catch(function (error_msg) {
        error_fun(error_msg.message);
        console.log('error catch: ', error_msg.message);
        hideLoading();
    });

}

function success(json) {
    pred_tag.style.display = "block";

    error_tag.style.display = "none";
    error.style.display = "none";
    if(json.prediction.isNegative == true){
        negative.style.display = "block";
        neg_tag.style.display = "block";
        negative.innerText = json.prediction.response;
    }else{
        positive.style.display = "block";
        pos_tag.style.display = "block";
        positive.innerText = json.prediction.response;
    }

    // negative.innerText = json.prediction.Negative.toFixed(2);
    // positive.innerText = json.prediction.Positive.toFixed(2);
}

function error_fun(message) {
    negative.style.display = "none";
    positive.style.display = "none";
    neg_tag.style.display = "none";
    pos_tag.style.display = "none";
    pred_tag.style.display='none';

    if(message != null){
        error.innerHTML = message;
        error_tag.style.display = "block";
        error.style.display = "block";
    }else{
        error_tag.style.display = "none";
        error.style.display = "none";
    }
}
