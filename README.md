### **🚨 CampusSafe – Campus Safety & Emergency System**



### **📌 Overview**



CampusSafe is a real-time emergency communication system designed to improve safety within campus environments. It enables administrators to instantly send alerts to students during emergencies such as fire, medical incidents, or security threats. The system ensures fast communication, clear instructions, and quick response.




### **🎯 Features**


🚨 **Real-Time Emergency Alerts**

📍 **Location-Based Alert Input**

🔴 **Full-Screen Alert with Animations**

📢 **Safety Instructions for Each Alert Type**

📳 **Vibration & Alarm Notifications**

✅ **Student Status Reporting (Safe / Need Help)**

📞 **Quick Emergency Call Feature**

📧 **Emergency Email Support**




### **🏗️ System Architecture**

The system follows a centralized architecture:

- **AlertManager (Singleton):** Stores alert data (type, location, timestamp)

- **Foreground Service:** Runs in background and broadcasts alerts

- **Broadcast System:** Sends real-time updates to all users

- **Student UI:** Receives and displays alerts




### **🧠 Core Components**



- AlertManager.kt – Central data controller

- AlertForegroundService.kt – Background service for alert monitoring

- AdminPanelActivity.kt – Admin interface for triggering alerts

- StudentPanelActivity.kt – Student interface for receiving alerts

- MainActivity.kt – Entry point with navigation and emergency actions

- AlertType.kt – Enum defining alert types and properties




### **🔄 Workflow**



> 1. Admin selects alert type and location

> 2. AlertManager updates alert data

> 3. Foreground service starts monitoring

> 4. Broadcast is sent to all users

> 5. Students receive alert with sound & animation

> 6. Students respond with their status




### **🎬 Animations & 🔗 Intents**



The app uses animations like blinking effects, color transitions, and scaling to grab user attention during emergencies. Intents are used for navigation between screens, making emergency calls, sending emails, and broadcasting alerts across the system.




### **⚙️ Technologies Used**



- > Kotlin

- > Android SDK

- > Material Design 3

- > Foreground Services

- > Broadcast Receivers




### **⚠️ Challenges & 🚀 Future Scope**



The system faces challenges such as maintaining real-time performance, ensuring background execution, and handling multiple alerts efficiently. In the future, features like GPS tracking, cloud storage, real-time analytics, and IoT integration can be added to enhance scalability and functionality.






### **📦 Installation**

git clone https://github.com/alanriju95-svg/CampusSafe.git

Open the project in Android Studio and run it on an emulator or device.



### **🤝 Contribution**


Contributions are welcome! Feel free to fork the repository and submit pull requests.




### **📄 License**


This project is for educational purposes.



### **👨‍💻 Author**


> ### **Your Name**

GitHub: https://github.com/alanriju95-svg




### **⭐ Support**


If you like this project, give it a ⭐ on GitHub!

