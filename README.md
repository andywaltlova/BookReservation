# LibraryApp <img align= "right" src="app/src/main/res/drawable/logo_knihovna.png" alt="drawing" width="300"/>

Application can be used to monitor requests for books that are in stocks of **Central Library of the Faculty of Arts** at Masaryk university. More about our library at https://knihovna.phil.muni.cz/ .

**When a new request for a book appears, the application sends a notification containing the location number of the book**, which makes it easier to find it in the library stocks.

## Why the application was created
The purpose of this application is to **make librarians work more efficient**. As a library, **we guarantee 20 minutes to process each book request**, so it's important for staff to find out about new requests as soon as possible, regardless of where they are in the library.
All book requests that are displayed in the application always have one of three colors - green, orange or red. Color indicates remaining time to process given book request in guaranteed time.
- &#x1F4D7; Green - plenty of time
- &#x1F4D2; Orange - half of the guaranteed time is gone
- &#x1F4D5; Red - guaranteed time has expired

## System Requirements
- Minimal SDK version: 26 (Android 8.0)
- Target SDK version: 30 (Android 10.0+)

For notifications to work properly, application needs to have permission to send notifications (that currently needs to be manually enabled by user in smartphones settings).
If you want the application to send notifications even if it runs in the background, you need to enable it in the appropriate settings (usually by default the system itself puts the application to sleep to save battery consumption).

## API response
The image below illustrates the API response that is used to retrieve current book reservations. For our purposes, the application uses the time, date, barcode and location number fields. **Currently there is option to set custom URL to API, but it will work only with exact same response structure as illustrated by image below**.
      
![API example](images/API_JSON_example.PNG)

## How the application looks

<img src="images/main_visual.jpg" alt="drawing" width="300"/> <img src="images/workDone_notif.jpg" alt="drawing" width="300"/>

<img src="images/settings_visual.jpg" alt="drawing" width="300"/> <img src="images/about_visual.jpg" alt="drawing" width="300"/>
