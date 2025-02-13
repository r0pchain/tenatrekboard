
# TenaControls Trek Board Controller APK

Refactored Android control application APK for TenaControls lighting control boards for 1/350-scale Enterprise models


## Overview
TenaControls makes some really cool lighting boards for scaled science-fiction models. Unfortunately, the Bluetooth apps they provide are exceptionally dated - and on Android, are starting to gripe pretty heavily when you attempt to install them. In fact, on the latest version of Android, you can't install without having an adb setup and manually installing with "**--bypass-low-target-sdk-block**" which approximately zero customers are going to want to do. 

I figured out how to communicate over Bluetooth with my own Trek-model Tena board by snooping Bluetooth and am offering this open source version to ensure these control boards continue to work with modern Android phones.

## FAQ
**Q: How can I install this?**

A: I typically keep it on my Google Drive and temporarily allow Google Drive as a source for APKs. This is also how I installed the manufacturer-provided TenaControls APK. Unfortunately, this method straight up will not work on Android 15 and above and requires you to go through adb hassle to get it on your phone. 

**Q: What version of Android do I need?**

A: I targeted Android 12 as the minimum. It's the oldest version still supported by Google. If you have an older version of this, you will be able to install the TenaControls app just fine without Android gripes. It's also the version that will properly prompt for the newfangled Bluetooth permissions structures in modern Android. 

**Q: Why is this a dev instead of release APK?**

A: Because I don't want to go through the hassle of signing it. If you don't trust my unsigned APK, the source code is there for you to build yourself in Android Studio!

**Q: Is this compatible with my TenaControls model board?**

A: If you have the TenaControls Bluetooth board for the [Enterprise 1/350 kit](https://tenacontrols.com/product/complete-android-bluetooth-controlled-lighting-board-and-system/)? Yes. Anything else? I have no clue and probably not. 

**Q: Why did you go this route?**

A: My first issue with the official TenaControls app started in October 2019. I let TenaControls know that likely all they'd need to do was recompile the app for the latest version of Android and wasn't met with much enthusiasm from them. I reached out again in 2024 when my new Pixel wouldn't run it at all without adb and bypassing the security block - once again letting them know the app needed an update - with no apparent interest from them in doing that, and a recommendation that I "keep an old Android device around" to circumvent the problem. 

**Q: What else have you done with this research?**

A: I never really liked the whole "locked app controls the lighting" experience with this board because I'm either demonstrating this to a visitor at my house and want to be able to talk through some of the cool detail of the model or I'm in my office and want to be able to just enjoy the model without fiddling on my phone. I've purchased a Pi Zero board and an implementing a "show and tell" Flask app that I will control with Home Assistant and using voice commands, will be able to turn on the model and have it cycle through lighting scenarios on my model much like a Hue bulb will allow you to set scenes. 

**Q: Can you send me the original TenaControls app?**

A: I cannot. TenaControls doesn't offer a public download of it and you get it if you buy a board from them via an APK sitting on a random Google Drive to your email. I suggest you reach out to them if you'd like a copy of the original. 

## The Tech Details
My first issue with the official TenaControls app started in October 2019 - Android was griping that the app "may not be compatible" back then. I let TenaControls know that likely all they'd need to do was recompile the app for the latest version of Android and wasn't met with much enthusiasm from them. I reached out again in 2024 when my new Pixel wouldn't run it at all without adb and bypassing the security block - once again letting them know the app needed an update and asked if I could just do it for them - with no apparent interest from them in doing that, and a recommendation that I "keep an old Android device around" to circumvent the problem. 

I suspected the app was super simple, so I decided to just start up the TenaControls app, sniff Bluetooth on my phone, and see what was going on when I interacted with my model - and had the answer in less than five minutes. All the TenaControls app does is connect to the previously-established paired Bluetooth MAC as a serial connection and send human-readable commands to it in the format **#COMMAND*** with the hash starting the command and the asterisk being the EOL; at this point, it was as easy as just clicking every button in the TenaControls app, capturing the command while snooping Bluetooth, and then reimplementing that command in a similarly super-simple - but more modern - Android app. 

## Parting Thoughts
I can't understand why TenaControls was unwilling to either update the app or to open-source it - as there is zero intellectual property in sending human-readable commands over a Bluetooth serial connection - but it is incredibly frustrating when expensive and boutique boards like this offer zero forward support and you start to wonder if you're boned on the next Android upgrade or have to keep an old phone/tablet around to make stuff continue to function. I am considering lighting other sci-fi models in the future and have to admit that this whole situation has definitely given me pause in purchasing another Tena board knowing they won't support it going forward and that it might not be as straightforward as snooping serial commands in future boards. 
