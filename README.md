# LeafApp

<p align="center">
  <img src="https://github.com/OwariYagami/leafApp/blob/Starter-Project/leafApp%20(1).png" alt="Screenshot 1" width="30%">
  <img src="https://github.com/OwariYagami/leafApp/blob/Starter-Project/leafApp%20(3).png" alt="Screenshot 2" width="30%">
  <img src="https://github.com/OwariYagami/leafApp/blob/Starter-Project/leafApp%20(2).png" alt="Screenshot 3" width="30%">
</p>

## Description
LeafApp is an Android application designed to identify leaf diseases using a TensorFlow Lite model. The app allows users to select a leaf image from their device gallery and process it to determine if the leaf is affected by disease (such as blight, powdery mildew) or is healthy.
## Features
- Automatic Classification: Utilizes a TensorFlow Lite model to analyze the leaf image and classify it into one of the predefined categories: blight, powdery mildew, or healthy.
- Image Selection: Choose a leaf image from your device’s gallery for analysis.
- Classification Results: Clearly displays the classification result, making it easy to understand the condition of the analyzed leaf.

## Technologies Used
- Kotlin
- Android Studio
- TensorFlow

## Installation
1. Clone the repository:
    ```bash
    git clone https://github.com/OwariYagami/leafApp.git
    ```
2. Open the project in Android Studio
3. Build and run the project on an emulator or physical device

## Usage
1. Launch the app on your device.
2. Select Image: Open the app and choose a leaf image from your device’s gallery by tapping the "Select Image" button.
3. Process and Classify: After selecting an image, the app will automatically process the image using the TensorFlow Lite model and display the classification result on the screen.
4. View Results: The classification result will indicate whether the leaf is affected by blight, powdery mildew, or is healthy.

## Contributing
1. Fork the repository
2. Create a new branch:
    ```bash
    git checkout -b feature-branch
    ```
3. Commit your changes:
    ```bash
    git commit -m 'Add some feature'
    ```
4. Push to the branch:
    ```bash
    git push origin feature-branch
    ```
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
