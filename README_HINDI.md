# Sharma Pharmacy Android App — नया प्रोजेक्ट

## शामिल सुविधाएँ
- Medicine search
- नमूना दवाएँ और local inventory
- Cart और अनुमानित बिल
- प्रति cart item 5%–80% discount validation
- Admin में नई दवा जोड़ना
- Stock और price update
- Batch/expiry विवरण
- Demo sale पर stock घटाना
- फोन में inventory सेव करना (SharedPreferences)

## Android Studio में खोलना
1. ZIP को Extract करें।
2. Android Studio में **File > Open** चुनें।
3. Extract किए गए `SharmaPharmacyAndroid` फ़ोल्डर को चुनें।
4. Gradle Sync पूरा होने दें।
5. अगर JDK माँगे, तो **Embedded JDK / JDK 17** चुनें।
6. Android फोन या Emulator चुनकर Run दबाएँ।

## जरूरी बातें
- Gradle Wrapper 8.9 और Android Gradle Plugin 8.7.3 उपयोग हुए हैं; पहली Sync में इंटरनेट से डाउनलोड होना जरूरी है।
- यदि `Connect timed out` आए तो यह ZIP की Java code समस्या नहीं, नेटवर्क/प्रॉक्सी/फ़ायरवॉल/डाउनलोड की समस्या हो सकती है। दूसरे Wi-Fi या मोबाइल hotspot से Try Again करें।
- यह पहला local starter version है। इसमें अभी वास्तविक online database, website sync, secure admin login, payment gateway, prescription upload, WhatsApp integration, GST-compliant invoice, cloud backup या multi-device sync लागू नहीं हैं।
- दवा का वास्तविक वितरण और prescription वाली दवाओं की बिक्री लागू कानून तथा फार्मासिस्ट की जाँच के अनुसार ही करें।
- यह e-commerce या eVitalRx का exact clone नहीं है; यह Sharma Pharmacy की अपनी शुरुआती app है।
