# A game of "Rock Paper Scissors", online

I have spent a week working on this simple online game. It's an implementation of
[Rock Paper Scissors](http://en.wikipedia.org/wiki/Rock-paper-scissors), that can be played online.

Your HTTP Session (created the first time you connect with a browser) will be your "authentication credential".
In other words, if you open 2 browsers, you can play against yourself. Yes, I was too lazy (and had no time) to implemnt
any registration/accounting.

Ah, and if the server shuts down, nothing is preserved (i.e. no database in this implementation).

## Screenshots

![Desktop Screenshot](https://raw.github.com/detro/rock-paper-scissors/master/README.pics/desktop.png)

![Mobile Screenshot](https://raw.github.com/detro/rock-paper-scissors/master/README.pics/mobile.png)

## Features

* Support for Player vs Player Matches (PvP)
* Support for Player vs Computer Matches (PvC)
* Ability to restart a game when finished
* Ability to play multiple games at the same time
* Support for multiple screen sizes (from Desktop to Smartphone) (i.e. "Responsive" design)
* Can be extended to support any number of weapons (see [rpsls](http://en.wikipedia.org/wiki/Rock-paper-scissors-lizard-Spock))
  by just changing a single matrix of numbers inside the class `Weapons`

## TODO

* Support for [Rock-Paper-Scissors-Lizard-Spock](http://en.wikipedia.org/wiki/Rock-paper-scissors-lizard-Spock)
* Keep track of personal stats (won/lost/draw games)
* Support for persistent/non-memory only games
* Support for accounts/login
* MORE testing

## Architecture

While the app is all run and served via a single web server, it's logically separate into 2 very specific parts: a
RESTful API (at `/api`) and a single-page web application (at `/`).

The RESTful API provides a set of calls to start and play matches. It looks as follows:
```
GET     /api/matches
            type        = ["all"|"available"|"mine"] - default = all
GET     /api/weapons
POST    /api/match
            kind        = ["pvp"|"pvc"] - default = pvp
GET     /api/match/:matchId
PUT     /api/match/:matchId
            action      = ["join"|"leave"|"restart"|"weapon"]
            weaponId    = [0|...|NUMBER_OF_POSSIBLE_WEAPONS-1]
```

The web application instead is built in an MVC fashion, and designed to be responsive (so it rearranges itself on
smaller screens). The models are a reflection of the status of the Matches server-side. Synchronization is done with
good-old polling: a bad choice but the only one offered by the chosen web framework. Should have probably been built
using [HTTP Comet](http://en.wikipedia.org/wiki/Comet_(programming)) or, even better,
[WebSockets](http://en.wikipedia.org/wiki/WebSocket).

## Dependencies

**Runtime (backend)**:

* [Java Spark micro-web-framework](http://www.sparkjava.com/)
* [Google Gson for the JSON](https://code.google.com/p/google-gson/)

**Runtime (frontend)**:

* [jQuery](http://jquery.com/)
* [Backbone.js](http://backbonejs.org/)
* [Underscore.js](http://underscorejs.org/)
* [Yahoo's Pure CSS](http://purecss.io/)
* [Font Awesome](http://fortawesome.github.io/Font-Awesome/)

**Testing**:

* [TestNG](http://testng.org/doc/index.html)
* [Apache Commons HTTP Client](http://hc.apache.org/httpclient-3.x/)

**Building**

* [Gradle](http://www.gradle.org/)

## License

**Public Domain**. Do whatever you want with the code. I don't care.

Please, DO NOT expect support for this project. This was a _Divertissement_
([look it up](http://en.wikipedia.org/wiki/Divertissement)) and I'm not even considering
this as a real project.
