# Landmark Remark Android App
# Overview
## Objective
_Build an app that can save and display simple notes at the current location on a map and be viewed by the note creator and other app users._
## Solve project problems
**Which platform should the application be developed on? What language to use?**
- Developed on Android native platform using Kotlin language.

**Where is the data stored?**
- Store data including users and notes in Firestore on Firebase.

**How to display the map?**
- Use google map api.

**What will the interface look like?**
- The interface should be designed to simplify user operations (I used figma to sketch the interface).

**How to authenticate users?**
- Use username (unique), password (previously, the initial idea would be to use the device's ip to determine the user's identity).

**What does a note need to store?**
- Store: noteId, userId, createdAt, location(lat, long), conttent.

## TechStack
- Language: [Android, Kotlin](https://developer.android.com/kotlin)
- Database: NoSql [Firestore](https://firebase.google.com)
- Design sketch: Figma [My sketch](https://www.figma.com/design/oJ0xNjb4aFhVyy66gwuhMU/Untitled?node-id=0-1&t=pcN12rJmmWZtwYVR-1)
- Service: [Google Maps Api](https://mapsplatform.google.com/)

## Feature
✅ _Login/Sign up a user account._

✅ _Add/View/Delete your own notes on the map._

✅ _View other users' nearby notes._

✅ _Search for usernames and notes by keywords and highlight the notes._

✅ _Customize the type of map you want to display._

✅ _Store that account login status on the device._

## Demo App
[![YouTube](https://img.icons8.com/color/48/000000/youtube-play.png)](https://youtu.be/MHeMTvu0uVY)

# Timeline
## 5 days
- **24/5:** _Learn more about Kotlin's coding structure in Android (Because in the past I mostly used Java to build Android apps)._
- **25/5:** _Continue learning about Kotlin's coding structure. And design sketch for the app._
- **27/5:** _Based on sketch to build app interface, item note on XML._
- **28/5:** _Connect to firestore, handle user account creation, lưu trữ thông tin người dùng trên firestore, display google map, create custom map type display, create notes and store notes to firestore._
- **29/5:** _Display notes on the map with self-designed items, handle deletion of notes, search for notes by keyword, save login status._

# Issues or Limitations
## Issues
- _I'm having difficulty determining a user's identity using the IP address of the user's mobile device._

**Solution:** I switched to using a unique username and encrypted password to identify the user.

- _Initially, when using the default Google Map marker, the user had to click on the marker to see what the note was doing and could not identify the owner of that note while in the full map view, causing difficulties in the user experience._

**Solution:** I custom designed an item to display notes on the map so users can preview the content and owner of the note.

- _When users enter keywords to search for notes, I cannot highlight the notes I am looking for in the main map._

**Solution:** I transmitted the keyword the user entered through the secondary map, then handle search and highlight appropriate notes before displaying it on the screen.
## Limitations
-_The biggest limitation is that every time you add a new note or delete a note, you have to clear the map first and then recall the note data._
-_The second limitation is the solution to the third problem above, which is that users will switch to the secondary map and not be able to highlight search results on the main map._


