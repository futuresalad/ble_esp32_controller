## Project description
This app was created for the app development lecture at MCI Medtech Master. The App project "Eiskaltes Händchen" is intended to control a 3D printed hand prosthesis over a BLE (bluetooth low energy) connections. 

## Implementations:
+ Bluetooth connectivity
+ QR code scanning
+ Grip pattern selection
+ Manual control of each finger
+ Database for user authentication
+ Switching between Night/Light-mode

## Operation
When starting the app for the first time, the user is prompted to grant location and positioning permissions required for accessing the bluetooth interface. Next, an overlay with a sign up form is presented. After successfully creating a user and logging in, the main menu is available. Pressing the "Connect device" checks if bluetooth is enabled and permissions for camera access are granted. A camera view lets the user scan the QR code presented on the OLED display of the ESP32 peripheral. The QR code contains the name under which the peripheral advertises for connection. After a successful QR code scan, the phone in the role as the BLE central, scans for connectable devices with the provided name. In case no matching device is found, a retry button can be pressed. When connected, the app returns to the main activity. 

If connected, the buttons to navigate to the "grip-patterns" and "manual-control" activities become available. In the manual control menu, the sliders, each representing one finger, can be dragged to different positions. The index and position of the slider is then communicated over the TX Characteristic of the Nordic UART service to the ESP32. A callback function of the ESP32 firmware handles the incoming data and adds the respective command to the work queue. Items in the work queue are then processed by a handler to control the finger motors. Upon receiving a command, the firmware prints the command to the serial monitor. In the "grip-patterns" menu, pre-defined hand poses can be selected with buttons. On button press, the app sends the respective command for each pattern to the ESP32. 
  

## Flashing the firmware to the ESP32
The firmware on the ESP32 uses Zephyr RTOS. A compiled and ready to flash version of the ESP32 firmware is included in this repository as firmware/bootloader.bin.
To flash the firmware on a ESP32, run following commands:

    python3 -m pip install esptool
    python3 -m esptool --chip esp32 --port <COM_PORT> -b 115200 write_flash -z 0x1000 bootloader.bin



  

