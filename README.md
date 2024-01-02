## Android app to control Zephyr OS Application on custom ESP32 board
This app was created for the app development lecture at MCI Medtech Master.
A compiled version of the ESP32 firmware is also included in this repository as firmware/bootloader.bin.
To flash the firmware on a ESP32, run following commands:

  python3 -m pip install esptool
  python3 -m esptool --chip esp32 --port <COM_PORT> -b 115200 write_flash -z 0x1000 bootloader.bin



  

