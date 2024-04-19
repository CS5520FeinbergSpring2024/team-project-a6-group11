# Music Diary

Music Diary is an Android app for creatives and music lovers to document the music they listen to,
allowing its users to create unique music diary entries for each day. Music Diary is integrated with
the Spotify Web API and Spotify’s Android SDK to allow users to log in or register with Spotify,
search for music to use in diary entries, and listen to songs/tracks from Spotify. Diary entries are
stored using Firebase Realtime Database, which is also used to store received diary messages.

## Features:

* Diary book: users can combine music, text, and icons for emotions to create unique diary entries
  for each day.
* Music search: users can browse and select music from Spotify to use in their diary entries.
* Shared diary: users can collaborate on diary entries and view public diary entries.
* Profile: users can edit their username and view their profile picture.
* Messaging: users can send diary entries to other users.

## Important notes:

* To log in to Music Diary, you will need a Spotify account. By default, apps registered with
  Spotify are in “development mode”, which means that in order to log in with your personal account,
  the name and email associated with the account needs to be whitelisted. Please email
  chin.jef@northeastern.edu if you want to use your personal Spotify account to log in. Otherwise,
  you can sign in to Spotify using the test accounts listed below.
* Some song previews are unavailable, possibly due to copyright restrictions. However, you can still
  create diary entries with these songs and add them to your diary.
* Songs are limited to 30-second previews as full-length tracks are only available to users with
  Spotify Premium, which none of our team members have to test with.
* To ensure app stability and security, Music Diary requires automatic date and time to be enabled.
  This is mainly to prevent users from manually changing the date and time to create multiple
  entries on the same day.
* After being redirected to the Spotify website to log out, users can then return to Music Diary by
  navigating to the app or pressing the back button to log back in.

## Test accounts:

* Email: musicdiaryuser1@outlook.com  
  Password: sugarforthepill1^  
  App username: musicdiaryuser1

* Email: musicdiaryuser2@outlook.com  
  Password: thescientist1^  
  App username: musicdiaryuser2
